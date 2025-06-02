package com.example.brewspot.view.history

import com.example.brewspot.view.payment.PaymentMethod
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

data class BookingHistory(
    val id: String = "",
    val cafeId: String = "",
    val cafeName: String = "",
    val userId: String = "",
    val userName: String = "",
    val date: String = "",
    val time: String = "",
    val totalGuests: Int = 0,
    val selectedTables: List<String> = emptyList(),
    val totalPrice: Double = 0.0,
    val items: List<Map<String, Any>> = emptyList(),
    val timestamp: Date? = null,
    val status: String = "Sudah Dibayar",
    val cafeImageUrl: String? = null,
    val appFeeAmount: Double = 0.0,
    val downpaymentAmount: Double = 0.0,
    val reservationCafeName: String? = null,
    val reservationId: String? = null,
    val reservationSelectedTables: List<String>? = null,
    val reservationTime: String? = null,
    val reservationTotalGuests: Int? = null,
    val voucherName: String? = null,
    val voucherPotongan: Double? = null,
    val paymentMethod: String? = null
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
                time = doc.getString("reservationTime") ?: (doc.getString("time") ?: ""),
                totalGuests = doc.getLong("reservationTotalGuests")?.toInt() ?: (doc.getLong("totalGuests")?.toInt() ?: 0),
                selectedTables = doc.get("reservationSelectedTables") as? List<String> ?: (doc.get("selectedTables") as? List<String> ?: emptyList()),
                totalPrice = doc.getDouble("totalPrice") ?: 0.0,
                items = doc.get("items") as? List<Map<String, Any>> ?: emptyList(),
                timestamp = doc.getTimestamp("timestamp")?.toDate(),
                status = doc.getString("status") ?: "Sudah Dibayar",
                cafeImageUrl = null,
                appFeeAmount = doc.getLong("appFeeAmount")?.toDouble() ?: 0.0,
                downpaymentAmount = doc.getLong("downpaymentAmount")?.toDouble() ?: 0.0,
                reservationCafeName = doc.getString("reservationCafeName"),
                reservationId = doc.getString("reservationId"),
                reservationSelectedTables = doc.get("reservationSelectedTables") as? List<String>,
                reservationTime = doc.getString("reservationTime"),
                reservationTotalGuests = doc.getLong("reservationTotalGuests")?.toInt(),
                voucherName = doc.getString("voucherName"),
                voucherPotongan = doc.getLong("voucherPotongan")?.toDouble(),
                paymentMethod = doc.getString("paymentMethodId")
            )
        }
    }
}