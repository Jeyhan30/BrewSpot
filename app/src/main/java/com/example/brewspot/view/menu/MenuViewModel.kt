// Brewspot/app/src/main/java/com/example/brewspot/view/menu/MenuViewModel.kt
package com.example.brewspot.view.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewspot.view.home.Cafe
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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

    private val _cartItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val cartItems: StateFlow<List<MenuItem>> = _cartItems.asStateFlow()

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()

    fun addToCart(menuItem: MenuItem) {
        _cartItems.update { currentItems ->
            val existingItem = currentItems.find { it.id == menuItem.id }
            if (existingItem != null) {
                currentItems.map {
                    if (it.id == menuItem.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                menuItem.copy(quantity = 1).let { newItem ->
                    currentItems + newItem
                }
            }
        }
        calculateTotalPrice()
    }

    fun removeFromCart(menuItem: MenuItem) {
        _cartItems.update { currentItems ->
            val existingItem = currentItems.find { it.id == menuItem.id }
            if (existingItem != null && existingItem.quantity > 1) {
                currentItems.map {
                    if (it.id == menuItem.id) it.copy(quantity = it.quantity - 1) else it
                }
            } else {
                currentItems.filter { it.id != menuItem.id }
            }
        }
        calculateTotalPrice()
    }

    private fun calculateTotalPrice() {
        _totalPrice.value = _cartItems.value.sumOf { it.price * it.quantity }
    }

    fun checkout() {
        viewModelScope.launch {
            if (_cartItems.value.isEmpty()) {
                println("Keranjang kosong, tidak ada yang bisa di-checkout.")
                return@launch
            }

            val currentUser = auth.currentUser
            if (currentUser == null) {
                println("Pengguna tidak terautentikasi. Tidak dapat melakukan checkout.")
                // TODO: Arahkan pengguna ke layar login atau tampilkan pesan kesalahan
                return@launch
            }

            try {
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                val username = userDoc.getString("username") ?: "Unknown User"
                val email = userDoc.getString("email") ?: currentUser.email ?: "Unknown Email"
                val phoneNumber = userDoc.getString("phoneNumber") ?: "N/A" // Asumsi ada field phoneNumber, jika tidak ada akan "N/A"

                println("Proses checkout dimulai untuk User: $username, Email: $email, Phone: $phoneNumber")

                val orderItems = _cartItems.value.map { item ->
                    hashMapOf(
                        "menuItemId" to item.id,
                        "name" to item.name,
                        "quantity" to item.quantity,
                        "priceAtOrder" to item.price
                    )
                }

                val orderHistory = hashMapOf(
                    "items" to orderItems,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "totalPrice" to _totalPrice.value,
                    "userId" to currentUser.uid,
                    "username" to username, // Tambahkan username
                    "userEmail" to email,   // Tambahkan email
                    "userPhone" to phoneNumber, // Tambahkan nomor telepon (jika tersedia)
                    "cafeId" to _cafeDetails.value?.id,



                )

                firestore.collection("history")
                    .add(orderHistory)
                    .await()

                println("Pesanan berhasil disimpan ke database history!")
                _cartItems.value = emptyList()
                _totalPrice.value = 0.0

            } catch (e: Exception) {
                println("Gagal menyimpan pesanan ke database: ${e.message}")
                // TODO: Tampilkan pesan kesalahan kepada pengguna
            }
        }
    }

    fun fetchCafeAndMenuItems(cafeId: String) {
        viewModelScope.launch {
            try {
                val cafeDoc = firestore.collection("Cafe").document(cafeId).get().await()
                if (cafeDoc.exists()) {
                    val cafe = Cafe.fromFirestore(cafeDoc)
                    _cafeDetails.value = cafe
                } else {
                    _cafeDetails.value = null
                    println("Cafe with ID $cafeId not found.")
                }

                firestore.collection("Cafe").document(cafeId).collection("menu")
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val items = querySnapshot.documents.map { MenuItem.fromFirestore(it) }
                        _menuItems.value = items
                    }
                    .addOnFailureListener { e ->
                        println("Error fetching menu items: $e")
                        _menuItems.value = emptyList()
                    }

            } catch (e: Exception) {
                println("Exception in fetchCafeAndMenuItems: ${e.message}")
                _cafeDetails.value = null
                _menuItems.value = emptyList()
            }
        }
    }
}