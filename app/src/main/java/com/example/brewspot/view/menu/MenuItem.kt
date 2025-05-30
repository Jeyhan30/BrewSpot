package com.example.brewspot.view.menu

import com.google.firebase.firestore.DocumentId // If you want to use a Firestore ID for the item itself (optional if in array)
import com.google.firebase.firestore.DocumentSnapshot

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double, // Using Long for price to avoid decimal issues with integers
    val imageUrl: String = "",
    var quantity: Int = 0,
    val cafeId: String = "" // Tambahkan properti ini
) {
    companion object {
        fun fromFirestore(doc: DocumentSnapshot): MenuItem {
            return MenuItem(
                id = doc.id,
                name = doc.getString("name") ?: "",
                description = doc.getString("deskripsi") ?: "",
                price = doc.getDouble("harga") ?: 0.0,
                imageUrl = doc.getString("gambar") ?: "",
                cafeId = ""            )
        }
    }
}