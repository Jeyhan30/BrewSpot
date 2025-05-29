package com.example.brewspot.view.register


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _registerState = MutableStateFlow<String?>(null)
    val registerState: StateFlow<String?> = _registerState

    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        val userMap = hashMapOf(
                            "email" to email,
                            "username" to username
                        )
                        firestore.collection("users").document(uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                _registerState.value = "success"

                            }
                            .addOnFailureListener {
                                _registerState.value = "Firestore Error: ${it.message}"
                            }
                    } else {
                        _registerState.value = "Auth Error: ${task.exception?.message}"
                    }
                }
        }
    }
}
