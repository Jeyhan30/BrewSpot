package com.example.brewspot.view.history

import com.example.brewspot.view.payment.PaymentMethod
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

data class BookingHistory(
    val id: String = "", // Document ID from Firestore
    val cafeId: String = "",
    val cafeName: String = "", // Default cafeName, bisa digantikan reservationCafeName
    val userId: String = "",
    val userName: String = "",
    val date: String = "", // Tanggal reservasi (e.g., 01/06/2025)
    val time: String = "", // Default time, bisa digantikan reservationTime
    val totalGuests: Int = 0, // Default totalGuests, bisa digantikan reservationTotalGuests
    val selectedTables: List<String> = emptyList(), // Default selectedTables, bisa digantikan reservationSelectedTables
    val totalPrice: Double = 0.0, // Total harga pesanan
    val items: List<Map<String, Any>> = emptyList(), // Daftar item menu yang dipesan
    val timestamp: Date? = null, // Timestamp saat pesanan dibuat
    val status: String = "Sudah Dibayar", // Status booking: "Sudah Dibayar", "Dibatalkan", "Kadaluarsa"
    val cafeImageUrl: String? = null, // URL gambar kafe (diambil dari koleksi Cafe)
    val appFeeAmount: Double = 0.0,
    val downpaymentAmount: Double = 0.0, // REVISI: huruf 'p' kecil
    val reservationCafeName: String? = null, // FIELD BARU
    val reservationId: String? = null, // FIELD BARU
    val reservationSelectedTables: List<String>? = null, // FIELD BARU
    val reservationTime: String? = null, // FIELD BARU
    val reservationTotalGuests: Int? = null, // FIELD BARU
    val voucherName: String? = null, // FIELD BARU
    val voucherPotongan: Double? = null, // FIELD BARU
    val paymentMethod: String? = null // FIELD BARU: Payment Method
) {
    companion object {
        fun fromFirestore(doc: DocumentSnapshot): BookingHistory {
            return BookingHistory(
                id = doc.id,
                cafeId = doc.getString("cafeId") ?: "",
                cafeName = doc.getString("cafeName") ?: "Unknown Cafe",
                userId = doc.getString("userId") ?: "",
                userName = doc.getString("username") ?: "Unknown User",
                date = doc.getString("date") ?: "",
                // Mengambil nilai untuk properti utama dari field reservasi baru jika ada
                // Agar konsisten di seluruh aplikasi (list dan detail)
                time = doc.getString("reservationTime") ?: (doc.getString("time") ?: ""),
                totalGuests = doc.getLong("reservationTotalGuests")?.toInt() ?: (doc.getLong("totalGuests")?.toInt() ?: 0),
                selectedTables = doc.get("reservationSelectedTables") as? List<String> ?: (doc.get("selectedTables") as? List<String> ?: emptyList()),
                totalPrice = doc.getDouble("totalPrice") ?: 0.0,
                items = doc.get("items") as? List<Map<String, Any>> ?: emptyList(),
                timestamp = doc.getTimestamp("timestamp")?.toDate(),
                status = doc.getString("status") ?: "Sudah Dibayar",
                cafeImageUrl = null, // Ini akan diisi oleh ViewModel
                appFeeAmount = doc.getLong("appFeeAmount")?.toDouble() ?: 0.0,
                downpaymentAmount = doc.getLong("downpaymentAmount")?.toDouble() ?: 0.0, // REVISI penulisan
                reservationCafeName = doc.getString("reservationCafeName"), // Ambil field baru
                reservationId = doc.getString("reservationId"), // Ambil field baru
                reservationSelectedTables = doc.get("reservationSelectedTables") as? List<String>, // Ambil field baru
                reservationTime = doc.getString("reservationTime"), // Ambil field baru
                reservationTotalGuests = doc.getLong("reservationTotalGuests")?.toInt(), // Ambil field baru
                voucherName = doc.getString("voucherName"), // Ambil field baru
                voucherPotongan = doc.getLong("voucherPotongan")?.toDouble(), // Ambil field baru
                paymentMethod = doc.getString("paymentMethodId") // Ambil field baru: paymentMethod
            )
        }
    }
}