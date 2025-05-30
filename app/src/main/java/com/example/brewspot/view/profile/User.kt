package com.example.brewspot.view.profile


import com.google.firebase.firestore.DocumentSnapshot

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val image: String = "" // Add imageUrl field
) {
    companion object {
        fun fromFirestore(doc: DocumentSnapshot): User {
            return User(
                uid = doc.id,
                username = doc.getString("username") ?: "",
                email = doc.getString("email") ?: "",
                phoneNumber = doc.getString("phoneNumber") ?: "",
                image = doc.getString("image") ?: "" // Retrieve imageUrl from Firestore
            )
        }
    }
}