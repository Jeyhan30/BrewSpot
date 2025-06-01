package com.example.brewspot.utils


import java.text.NumberFormat
import java.util.Locale

// Fungsi formatRupiah yang dapat digunakan di seluruh aplikasi
fun formatRupiah(number: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(number)
}