package com.example.brewspot.view.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

data class PaymentMethod(
    val id: String,
    val name: String,
    val imageUrl: String
) {
    companion object {
        fun fromFirestore(doc: com.google.firebase.firestore.DocumentSnapshot): PaymentMethod {
            return PaymentMethod(
                id = doc.id,
                name = doc.getString("name") ?: "",
                imageUrl = doc.getString("gambar") ?: ""
            )
        }
    }
}

class PaymentMethodViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(emptyList())
    val paymentMethods: StateFlow<List<PaymentMethod>> = _paymentMethods

    private val _selectedPaymentMethod = MutableStateFlow<PaymentMethod?>(null)
    val selectedPaymentMethod: StateFlow<PaymentMethod?> = _selectedPaymentMethod

    init {
        fetchPaymentMethods()
    }

    fun fetchPaymentMethods() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("payment").get().await()
                val methodList = snapshot.documents.map { doc ->
                    PaymentMethod.fromFirestore(doc)
                }
                _paymentMethods.value = methodList
                Log.d("PaymentMethodViewModel", "Fetched ${methodList.size} payment methods.")
            } catch (e: Exception) {
                Log.e("PaymentMethodViewModel", "Error fetching payment methods: ${e.message}", e)
                _paymentMethods.value = emptyList()
            }
        }
    }

    fun selectPaymentMethod(method: PaymentMethod?) {
        _selectedPaymentMethod.value = method
        Log.d("PaymentMethodViewModel", "Selected payment method: ${method?.name ?: "None"}")
    }
}