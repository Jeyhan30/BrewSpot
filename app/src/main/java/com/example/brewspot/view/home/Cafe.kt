package com.example.brewspot.view.home

import com.google.firebase.firestore.DocumentSnapshot

data class Cafe(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val jamOperasional: String = "",
    val image: String = "" // This will contain a single Base64 string
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