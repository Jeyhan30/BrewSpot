// app/src/main/java/com/example/brewspot/view/home/Cafe.kt
package com.example.brewspot.view.home

import com.example.brewspot.view.menu.MenuItem
import com.google.firebase.firestore.DocumentSnapshot

data class Cafe(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val imagedetail: String = "",
    val jamOperasional: String = "",
    val image: String = "", // Untuk gambar potret/logo
    val imageDetail: String = "", // Untuk gambar landscape/detail di atas
    val menuItems: List<MenuItem> = emptyList(),
    val denahImage: String = "",
    val priceRange: String = ""

) {
    companion object {
        fun fromFirestore(doc: DocumentSnapshot): Cafe {
            return Cafe(
                id = doc.id,
                name = doc.getString("Name") ?: "",
                address = doc.getString("Address") ?: "",
                jamOperasional = doc.getString("jam_operasional") ?: "",
                image = doc.getString("image") ?: "", // Ambil dari field 'image'
                imageDetail = doc.getString("imagedetail") ?: "",
                denahImage = doc.getString("denah") ?: "",
                priceRange = doc.getString("priceRange") ?: ""// Ambil dari field 'image_detail'
            )
        }
    }
}