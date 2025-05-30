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

    // Cart items for the current session.
    // Each MenuItem in this list will now correctly have its cafeId set.
    private val _cartItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val cartItems: StateFlow<List<MenuItem>> = _cartItems.asStateFlow()

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()

    // Store the currently active cafeId in the ViewModel
    private var currentCafeId: String? = null

    fun addToCart(menuItem: MenuItem) {
        _cartItems.update { currentItems ->
            // Ensure the menuItem added to cart has the correct cafeId
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
    fun checkout(cafeId: String) { // MODIFIED: Accept cafeId
        viewModelScope.launch {
            val itemsToCheckout = _cartItems.value.filter { it.cafeId == cafeId } // MODIFIED: Filter by cafeId

            if (itemsToCheckout.isEmpty()) {
                println("Keranjang kosong untuk kafe ini, tidak ada yang bisa di-checkout.")
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
                val phoneNumber = userDoc.getString("phoneNumber") ?: "N/A"

                println("Proses checkout dimulai untuk User: $username, Email: $email, Phone: $phoneNumber")

                val orderItems = itemsToCheckout.map { item -> // MODIFIED: Use filtered items
                    hashMapOf(
                        "menuItemId" to item.id,
                        "name" to item.name,
                        "quantity" to item.quantity,
                        "priceAtOrder" to item.price,
                        "cafeId" to item.cafeId // Ensure cafeId is saved with each item
                    )
                }

                val orderHistory = hashMapOf(
                    "items" to orderItems,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "totalPrice" to itemsToCheckout.sumOf { it.price * it.quantity }, // MODIFIED: Use filtered total
                    "userId" to currentUser.uid,
                    "username" to username,
                    "userEmail" to email,
                    "userPhone" to phoneNumber,
                    "cafeId" to cafeId // Use the cafeId for the order
                )

                firestore.collection("history")
                    .add(orderHistory)
                    .await()

                println("Pesanan berhasil disimpan ke database history!")
                _cartItems.update { currentItems ->
                    currentItems.filterNot { it.cafeId == cafeId } // MODIFIED: Remove only the checked out items
                }
                calculateTotalPrice() // Recalculate total price after removing items

            } catch (e: Exception) {
                println("Gagal menyimpan pesanan ke database: ${e.message}")
                // TODO: Tampilkan pesan kesalahan kepada pengguna
            }
        }
    }

    fun fetchCafeAndMenuItems(cafeId: String) {
        currentCafeId = cafeId // Set the current cafeId
        // Optionally, clear the cart when switching cafes if you want separate carts per cafe session.
        // If you want a global cart that can contain items from multiple cafes, remove this line.
        // _cartItems.value = emptyList()
        // _totalPrice.value = 0.0

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
                        val items = querySnapshot.documents.map { doc ->
                            // When creating MenuItem from Firestore, pass the cafeId
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
                _menuItems.value = emptyList()
            }
        }
    }
}