package com.example.brewspot.view.voucher

import com.google.firebase.firestore.DocumentSnapshot

data class Voucher(
    val id: String = "", // ID Dokumen Firestore
    val name: String = "",
    val potongan: Double = 0.0, // Potongan dalam bentuk nominal (misal: 10000 untuk 10rb)
    val minimal: Double = 0.0 // Minimal pembelian
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