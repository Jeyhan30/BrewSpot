// app/src/main/java/com/example/brewspot/view/cafe_detail/CafeDetailViewModel.kt
package com.example.brewspot.view.cafe_detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewspot.view.home.Cafe
import com.example.brewspot.view.profile.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CafeDetailViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _cafe = MutableStateFlow<Cafe?>(null)
    val cafe: StateFlow<Cafe?> = _cafe

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun fetchCafeDetails(cafeId: String) {
        viewModelScope.launch {
            try {
                val documentSnapshot = firestore.collection("Cafe")
                    .document(cafeId)
                    .get()
                    .await()

                if (documentSnapshot.exists()) {
                    _cafe.value = Cafe.fromFirestore(documentSnapshot)
                    Log.d("CafeDetailViewModel", "Cafe data fetched: ${_cafe.value}")
                } else {
                    Log.d("CafeDetailViewModel", "Cafe document does not exist for ID: $cafeId")
                    _cafe.value = null
                }
            } catch (e: Exception) {
                Log.e("CafeDetailViewModel", "Error fetching cafe data: ${e.message}", e)
                _cafe.value = null
            }
        }
    }

    fun fetchCurrentUser() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            viewModelScope.launch {
                try {
                    val documentSnapshot = firestore.collection("users")
                        .document(userId)
                        .get()
                        .await()

                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)
                        _currentUser.value = user
                        Log.d("CafeDetailViewModel", "User data fetched: $user for UID: $userId")
                    } else {
                        val firebaseUser = auth.currentUser
                        _currentUser.value = User(
                            email = firebaseUser?.email ?: "",
                            username = firebaseUser?.displayName ?: firebaseUser?.email?.substringBefore('@') ?: "User",
                            phoneNumber = "", // Default empty
                            image = "" // Default empty
                        )
                        Log.d("CafeDetailViewModel", "User document does not exist for UID: $userId. Created temporary User object.")
                    }
                } catch (e: Exception) {
                    Log.e("CafeDetailViewModel", "Error fetching current user data: ${e.message}", e)
                    _currentUser.value = null
                }
            }
        } else {
            _currentUser.value = null
            Log.d("CafeDetailViewModel", "No authenticated user found for fetching current user data.")
        }
    }


    fun createReservation(
        cafeId: String,
        cafeName: String,
        userName: String,
        date: String,
        totalGuests: Int,
        time: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val reservationData = hashMapOf(
            "cafeId" to cafeId,
            "cafeName" to cafeName,
            "userId" to auth.currentUser?.uid,
            "userName" to userName,
            "date" to date,
            "totalGuests" to totalGuests,
            "time" to time,
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp() // Add server timestamp
        )

        firestore.collection("reservations")
            .add(reservationData)
            .addOnSuccessListener {
                Log.d("CafeDetailViewModel", "Reservation created successfully for $cafeName on $date at $time")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("CafeDetailViewModel", "Error creating reservation: ${e.message}", e)
                onFailure(e.localizedMessage ?: "Failed to create reservation.")
            }
    }
}