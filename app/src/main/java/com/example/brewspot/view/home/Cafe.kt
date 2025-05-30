package com.example.brewspot.view.home

import com.example.brewspot.view.menu.MenuItem
import com.google.firebase.firestore.DocumentSnapshot

data class Cafe(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val imagedetail: String = "",
    val jamOperasional: String = "",
    val image: String = "",
    val menuItems: List<MenuItem> = emptyList() // Add this line for menu items

) {
    companion object {
        fun fromFirestore(doc: DocumentSnapshot): Cafe {
            // Safely cast the 'image' field to a String
            val imageUrl = doc.get("image") as? String ?: ""

            return Cafe(
                id = doc.id,
                name = doc.getString("Name") ?: "",
                address = doc.getString("Address") ?: "",
                jamOperasional = doc.getString("jam_operasional") ?: "",
                image = imageUrl // Assign the safely casted string
            )
        }
    }
}