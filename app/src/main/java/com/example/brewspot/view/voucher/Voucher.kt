package com.example.brewspot.view.voucher

import com.google.firebase.firestore.DocumentSnapshot

data class Voucher(
    val id: String = "",
    val name: String = "",
    val potongan: Double = 0.0,
    val minimal: Double = 0.0
) {
    companion object {
        fun fromFirestore(doc: DocumentSnapshot): Voucher {
            return Voucher(
                id = doc.id,
                name = doc.getString("name") ?: "",
                potongan = doc.getDouble("potongan") ?: 0.0,
                minimal = doc.getDouble("minimal") ?: 0.0
            )
        }
    }
}