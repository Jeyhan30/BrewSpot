package com.example.brewspot.view.menu

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double,
    val imageUrl: String = "",
    var quantity: Int = 0,
    val cafeId: String = ""
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