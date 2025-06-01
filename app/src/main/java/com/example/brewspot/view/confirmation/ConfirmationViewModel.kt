package com.example.brewspot.view.confirmation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewspot.view.home.Cafe
import com.example.brewspot.view.menu.MenuItem
import com.example.brewspot.view.menu.MenuViewModel
import com.example.brewspot.view.payment.PaymentMethod
import com.example.brewspot.view.reservationTest.TableViewModel
import com.example.brewspot.view.voucher.Voucher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ConfirmationViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _cafeDetails = MutableStateFlow<Cafe?>(null)
    val cafeDetails: StateFlow<Cafe?> = _cafeDetails

    private val _reservationDetails = MutableStateFlow<Map<String, Any>?>(null)
    val reservationDetails: StateFlow<Map<String, Any>?> = _reservationDetails

    private val _orderedMenuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val orderedMenuItems: StateFlow<List<MenuItem>> = _orderedMenuItems

    private val _totalPayment = MutableStateFlow(0.0)
    val totalPayment: StateFlow<Double> = _totalPayment

    // Fixed Biaya Aplikasi
    val appFeeAmount = 2000.0     // Rp 2.000

    // Down Payment Percentage
    private val downPaymentPercentage = 0.5 // 50%

    // NEW: State for calculated down payment
    private val _calculatedDownPayment = MutableStateFlow(0.0)
    val calculatedDownPayment: StateFlow<Double> = _calculatedDownPayment

    var onFinalCheckoutSuccess: (() -> Unit)? = null
    var onFinalCheckoutFailure: ((String) -> Unit)? = null

    private lateinit var tableViewModel: TableViewModel

    private val _appliedVoucher = MutableStateFlow<Voucher?>(null)
    val appliedVoucher: StateFlow<Voucher?> = _appliedVoucher
    fun setTableViewModel(viewModel: TableViewModel) {
        this.tableViewModel = viewModel
    }
    fun applyVoucher(voucher: Voucher?) {
        _appliedVoucher.value = voucher
        recalculateTotals() // Panggil ini setiap kali voucher diterapkan atau dihapus
    }

    private fun recalculateTotals() { //
        val totalMenuOrderPrice = orderedMenuItems.value.sumOf { it.price * it.quantity } //
        var baseTotal = totalMenuOrderPrice + appFeeAmount //

        _appliedVoucher.value?.let { voucher -> //
            if (baseTotal >= voucher.minimal) { //
                baseTotal -= voucher.potongan //
                if (baseTotal < 0) baseTotal = 0.0 // Pastikan total tidak negatif
            }
        }

        val dpAmount = baseTotal * downPaymentPercentage //
        _calculatedDownPayment.value = dpAmount //
        _totalPayment.value = baseTotal - dpAmount //
    }
    fun fetchConfirmationDetails(cafeId: String, reservationId: String, menuViewModel: MenuViewModel) {
        viewModelScope.launch {
            Log.d("ConfViewModel", "Fetching confirmation details for Cafe ID: $cafeId, Reservation ID: $reservationId")
            try {
                val cafeDoc = db.collection("Cafe").document(cafeId).get().await()
                if (cafeDoc.exists()) {
                    _cafeDetails.value = Cafe.fromFirestore(cafeDoc)
                    Log.d("ConfViewModel", "Cafe details fetched: ${_cafeDetails.value?.name}")
                } else {
                    Log.e("ConfViewModel", "Cafe not found for ID: $cafeId")
                    _cafeDetails.value = null
                }

                val reservationDoc = db.collection("reservations").document(reservationId).get().await()
                if (reservationDoc.exists()) {
                    _reservationDetails.value = reservationDoc.data
                    Log.d("ConfViewModel", "Reservation details fetched: ${_reservationDetails.value?.get("userName")}")
                } else {
                    Log.e("ConfViewModel", "Reservation not found for ID: $reservationId")
                    _reservationDetails.value = null
                }

                val currentCartItems = menuViewModel.cartItems.value.filter { it.cafeId == cafeId }
                _orderedMenuItems.value = currentCartItems
                Log.d("ConfViewModel", "Current cart items from MenuViewModel: ${currentCartItems.size} items")

                val totalMenuOrderPrice = currentCartItems.sumOf { it.price * it.quantity }
                Log.d("ConfViewModel", "Total order price from menu items: $totalMenuOrderPrice")


                var baseTotal = totalMenuOrderPrice + appFeeAmount
                Log.d("ConfViewModel", "Base total (menu + app fee): $baseTotal")

                _appliedVoucher.value?.let { voucher ->
                    if (baseTotal >= voucher.minimal) {
                        baseTotal -= voucher.potongan
                        if (baseTotal < 0) baseTotal = 0.0 // Pastikan total tidak negatif
                    }
                }

                val dpAmount = baseTotal * downPaymentPercentage
                _calculatedDownPayment.value = dpAmount
                _totalPayment.value = baseTotal - dpAmount
            } catch (e: Exception) {
                Log.e("ConfViewModel", "Error fetching confirmation details: ${e.message}", e)
            }
            recalculateTotals()
        }
    }

    fun performCheckout(cafeId: String, reservationId: String, orderedItems: List<MenuItem>,selectedPaymentMethod: PaymentMethod?) {
        viewModelScope.launch {
            Log.d("ConfViewModel", "Performing checkout...")
            val currentUser = auth.currentUser
            if (currentUser == null) {
                onFinalCheckoutFailure?.invoke("User not authenticated.")
                return@launch
            }

            // Re-calculate totals to ensure consistency
            val totalMenuOrderPrice = orderedItems.sumOf { it.price * it.quantity }
            var baseTotal = totalMenuOrderPrice + appFeeAmount

            // Apply voucher discount to baseTotal before calculating down payment
            _appliedVoucher.value?.let { voucher ->
                if (baseTotal >= voucher.minimal) {
                    baseTotal -= voucher.potongan
                    if (baseTotal < 0) baseTotal = 0.0
                }
            }

            val dpAmount = baseTotal * downPaymentPercentage
            val grandTotal = baseTotal - dpAmount

            if (orderedItems.isEmpty()) {
                if (grandTotal <= 0.1 && grandTotal >= -0.1) {
                    Log.d("ConfViewModel", "Proceeding with checkout with no menu items, and total is approximately zero.")
                } else {
                    onFinalCheckoutFailure?.invoke("Tidak ada item menu untuk di-checkout.")
                    return@launch
                }
            }

            try {
                val userDoc = db.collection("users").document(currentUser.uid).get().await()
                val username = userDoc.getString("username") ?: "Unknown User"
                val email = userDoc.getString("email") ?: currentUser.email ?: "Unknown Email"
                val phoneNumber = userDoc.getString("phoneNumber") ?: "N/A"
                val cafeName = _cafeDetails.value?.name ?: "Unknown Cafe"

                // --- NEW: Fetch reservation details here ---
                val reservationDoc = db.collection("reservations").document(reservationId).get().await()
                val reservationDetails = if (reservationDoc.exists()) {
                    reservationDoc.data // Get all data from the reservation document
                } else {
                    null
                }
                // --- END NEW ---

                val orderItemsData = orderedItems.map { item ->
                    hashMapOf(
                        "menuItemId" to item.id,
                        "name" to item.name,
                        "quantity" to item.quantity,
                        "priceAtOrder" to item.price,
                        "cafeId" to item.cafeId,
                        "imageUrl" to item.imageUrl,
                        "description" to item.description
                    )
                }

                val orderHistory = hashMapOf(
                    "items" to orderItemsData,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "totalPrice" to grandTotal,
                    "userId" to currentUser.uid,
                    "username" to username,
                    "userEmail" to email,
                    "userPhone" to phoneNumber,
                    "cafeId" to cafeId,
                    "cafeName" to cafeName,
                    "reservationId" to reservationId,
                    "totalMenuOrderPrice" to totalMenuOrderPrice,
                    "appFeeAmount" to appFeeAmount,
                    "downPaymentAmount" to dpAmount,
                    "voucherPotongan" to (_appliedVoucher.value?.potongan ?: 0.0),
                    "voucherName" to (_appliedVoucher.value?.name ?: "")
                )
                selectedPaymentMethod?.let { method ->
                    orderHistory["paymentMethodId"] = method.id
                } ?: run {
                    orderHistory["paymentMethodId"] = "No Method Selected"
                    orderHistory["paymentMethodName"] = "Tidak ada metode pembayaran dipilih"
                    orderHistory["paymentMethodImageUrl"] = ""
                }

                // --- NEW: Add reservation details to orderHistory ---
                if (reservationDetails != null) {
                    orderHistory["reservationCafeName"] = reservationDetails["cafeName"] ?: "Unknown"
                    orderHistory["date"] = reservationDetails["date"] ?: "N/A"
                    orderHistory["reservationTime"] = reservationDetails["time"] ?: "N/A"
                    orderHistory["reservationTotalGuests"] = reservationDetails["totalGuests"] ?: 0
                    orderHistory["reservationSelectedTables"] = reservationDetails["selectedTables"] ?: emptyList<String>()
                    // Anda bisa menambahkan detail lain dari reservasi jika diperlukan
                }
                // --- END NEW ---

                db.collection("history")
                    .add(orderHistory)
                    .addOnSuccessListener { documentReference ->
                        Log.d("ConfViewModel", "Pesanan berhasil disimpan ke database history dengan ID: ${documentReference.id}!")

                        viewModelScope.launch {
                            try {
                                // The reservationDoc is already fetched above, no need to fetch again
                                // val reservationDoc = db.collection("reservations").document(reservationId).get().await()
                                if (reservationDoc.exists()) { // Use the already fetched reservationDoc
                                    val selectedTables = reservationDoc.get("selectedTables") as? List<String>
                                    selectedTables?.forEach { tableId ->
                                        db.collection("Cafe").document(cafeId)
                                            .collection("Table").document(tableId)
                                            .update("Book", true)
                                            .addOnSuccessListener {
                                                Log.d("ConfViewModel", "Meja $tableId berhasil dibooking di cafe $cafeId!")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("ConfViewModel", "Error booking meja $tableId di cafe $cafeId: ${e.message}", e)
                                            }
                                    }
                                    onFinalCheckoutSuccess?.invoke()
                                    if (::tableViewModel.isInitialized) {
                                        tableViewModel.triggerResetTableState()
                                    }

                                } else {
                                    Log.e("ConfViewModel", "Reservasi tidak ditemukan untuk membooking meja.")
                                    onFinalCheckoutFailure?.invoke("Reservasi tidak ditemukan untuk membooking meja.")
                                }
                            } catch (e: Exception) {
                                Log.e("ConfViewModel", "Gagal membooking meja: ${e.message}", e)
                                onFinalCheckoutFailure?.invoke("Gagal membooking meja: ${e.localizedMessage}")
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("ConfViewModel", "Gagal menyimpan pesanan ke database: ${e.message}", e)
                        onFinalCheckoutFailure?.invoke(e.localizedMessage ?: "Gagal menyimpan pesanan.")
                    }

            } catch (e: Exception) {
                Log.e("ConfViewModel", "Gagal melakukan checkout keseluruhan: ${e.message}", e)
                onFinalCheckoutFailure?.invoke(e.localizedMessage ?: "Gagal melakukan checkout.")
            }
        }
    }
}