package com.example.brewspot.view.voucher


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

class VoucherViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _vouchers = MutableStateFlow<List<Voucher>>(emptyList())
    val vouchers: StateFlow<List<Voucher>> = _vouchers

    private val _selectedVoucher = MutableStateFlow<Voucher?>(null)
    val selectedVoucher: StateFlow<Voucher?> = _selectedVoucher

    init {
        fetchVouchers()
    }

    fun fetchVouchers() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("voucher").get().await()
                val voucherList = snapshot.documents.map { doc ->
                    Voucher.fromFirestore(doc)
                }
                _vouchers.value = voucherList
                Log.d("VoucherViewModel", "Fetched ${voucherList.size} vouchers.")
            } catch (e: Exception) {
                Log.e("VoucherViewModel", "Error fetching vouchers: ${e.message}", e)
                _vouchers.value = emptyList()
            }
        }
    }

    fun selectVoucher(voucher: Voucher?) {
        _selectedVoucher.value = voucher
        Log.d("VoucherViewModel", "Selected voucher: ${voucher?.name ?: "None"}")
    }
}