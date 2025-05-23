package com.example.brewspot.repository


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    fun loginWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    firestore.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            val username = doc.getString("username") ?: "User"
                            onResult(true, username)
                        }
                        .addOnFailureListener {
                            onResult(false, null)
                        }
                } else {
                    onResult(false, null)
                }
            }
    }

    fun loginWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                firestore.collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val username = doc.getString("username") ?: "User"
                        onResult(true, username)
                    }
                    .addOnFailureListener {
                        onResult(false, null)
                    }
            } else {
                onResult(false, null)
            }
        }
    }

    fun saveGoogleUserToFirestore(user: FirebaseUser, onResult: (Boolean) -> Unit) {
        val db = firestore.collection("users").document(user.uid)

        db.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                // User belum ada, tambahkan
                val userData = hashMapOf(
                    "uid" to user.uid,
                    "email" to user.email,
                    "username" to (user.displayName ?: user.email?.substringBefore('@') ?: "Unknown")
                )
                db.set(userData)
                    .addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { onResult(false) }
            } else {
                // Sudah ada, tidak perlu simpan ulang
                onResult(true)
            }
        }.addOnFailureListener {
            onResult(false)
        }
    }

}
