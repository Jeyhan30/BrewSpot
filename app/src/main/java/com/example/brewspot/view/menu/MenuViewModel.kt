package com.example.brewspot.view.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewspot.view.home.Cafe
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MenuViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _cafeDetails = MutableStateFlow<Cafe?>(null)
    val cafeDetails: StateFlow<Cafe?> = _cafeDetails

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems
    private val db = Firebase.firestore

    private val _cartItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val cartItems: StateFlow<List<MenuItem>> = _cartItems.asStateFlow()

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()

    private var currentCafeId: String? = null
    private var _currentCafeName: String? = null
    private var _currentReservationId: String? = null

    var onCheckoutSuccess: ((String, String) -> Unit)? = null

    fun setReservationId(reservationId: String) {
        _currentReservationId = reservationId
    }
    fun getCurrentReservationId(): String? {
        return _currentReservationId
    }
    fun clearCartForCafe(cafeId: String) {
        _cartItems.update { currentItems ->
            currentItems.filterNot { it.cafeId == cafeId }
        }
        calculateTotalPrice()
    }
    fun addToCart(menuItem: MenuItem) {
        _cartItems.update { currentItems ->
            val itemWithCafeId = menuItem.copy(cafeId = currentCafeId ?: "")

            val existingItem = currentItems.find { it.id == itemWithCafeId.id && it.cafeId == itemWithCafeId.cafeId }
            if (existingItem != null) {
                currentItems.map {
                    if (it.id == itemWithCafeId.id && it.cafeId == itemWithCafeId.cafeId) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                itemWithCafeId.copy(quantity = 1).let { newItem ->
                    currentItems + newItem
                }
            }
        }
        calculateTotalPrice()
    }

    fun removeFromCart(menuItem: MenuItem) {
        _cartItems.update { currentItems ->
            val existingItem = currentItems.find { it.id == menuItem.id && it.cafeId == menuItem.cafeId }
            if (existingItem != null && existingItem.quantity > 1) {
                currentItems.map {
                    if (it.id == menuItem.id && it.cafeId == menuItem.cafeId) it.copy(quantity = it.quantity - 1) else it
                }
            } else {
                currentItems.filter { it.id != menuItem.id || it.cafeId != menuItem.cafeId }
            }
        }
        calculateTotalPrice()
    }

    private fun calculateTotalPrice() {
        _totalPrice.value = _cartItems.value.sumOf { it.price * it.quantity }
    }
    fun fetchCafeAndMenuItems(cafeId: String) {
        currentCafeId = cafeId
        viewModelScope.launch {
            try {
                val cafeDoc = firestore.collection("Cafe").document(cafeId).get().await()
                if (cafeDoc.exists()) {
                    val cafe = Cafe.fromFirestore(cafeDoc)
                    _cafeDetails.value = cafe
                    _currentCafeName = cafe.name
                } else {
                    _cafeDetails.value = null
                    _currentCafeName = null
                    println("Cafe with ID $cafeId not found.")
                }

                firestore.collection("Cafe").document(cafeId).collection("menu")
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val items = querySnapshot.documents.map { doc ->
                            MenuItem.fromFirestore(doc).copy(cafeId = cafeId)
                        }
                        _menuItems.value = items
                    }
                    .addOnFailureListener { e ->
                        println("Error fetching menu items: $e")
                        _menuItems.value = emptyList()
                    }

            } catch (e: Exception) {
                println("Exception in fetchCafeAndMenuItems: ${e.message}")
                _cafeDetails.value = null
                _currentCafeName = null
                _menuItems.value = emptyList()
            }
        }
    }
}