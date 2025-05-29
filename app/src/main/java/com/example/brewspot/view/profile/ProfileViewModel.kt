package com.example.brewspot.view.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewspot.view.profile.User // Pastikan Anda mengimpor kelas User dari package yang benar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
class ProfileViewModel : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchUserProfile()
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                fetchUserProfile()
            } else {
                _currentUser.value = null
                Log.d("ProfileViewModel", "User logged out, clearing profile data.")
            }
        }
    }

    fun refreshUserProfile() {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
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
                        Log.d("ProfileViewModel", "User data fetched: $user for UID: $userId")
                    } else {
                        val firebaseUser = auth.currentUser
                        _currentUser.value = User(
                            email = firebaseUser?.email ?: "",
                            username = firebaseUser?.displayName ?: firebaseUser?.email?.substringBefore('@') ?: "User",
                            phoneNumber = "", // Default empty
                            image = "" // Default empty
                        )
                        Log.d("ProfileViewModel", "User document does not exist for UID: $userId. Created temporary User object.")
                    }
                } catch (e: Exception) {
                    _currentUser.value = null
                    Log.e("ProfileViewModel", "Error fetching user data: ${e.message}", e)
                }
            }
        } else {
            _currentUser.value = null
            Log.d("ProfileViewModel", "No authenticated user found.")
        }
    }

    /**
     * Updates the user's profile information in Firestore.
     * Only updates username and phoneNumber as per your User data class.
     * The email field from Auth is no longer updated here.
     *
     * @param username The new username.
     * @param phoneNumber The new phone number (can be null or blank).
     * @param onSuccess Callback for successful update.
     * @param onFailure Callback for failed update with an error message.
     */
    fun updateProfile(
        username: String,
        phoneNumber: String, // <--- CHANGED: now takes phoneNumber, email not directly updated here
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            onFailure("User not logged in.")
            return
        }

        viewModelScope.launch {
            try {
                // We no longer update Firebase Auth email here, as the field in UI changed to phone number.
                // If you still need email to be updated (e.g., if it's displayed but not editable),
                // you would need to get it from currentUser?.email or pass it as a parameter if editable elsewhere.

                // Prepare data for Firestore update
                val updates = mutableMapOf<String, Any>(
                    "username" to username
                    // 'email' will remain as is in Firestore unless specifically passed/updated
                    // If you want to explicitly save email (even if not edited on this screen),
                    // you'd add: "email" to (currentUser?.email ?: firebaseUser.email ?: "")
                )
                // Add phone number
                updates["phoneNumber"] = phoneNumber // Save the new phone number

                // Update Firestore document
                firestore.collection("users").document(firebaseUser.uid)
                    .update(updates)
                    .await()

                // Refresh local _currentUser StateFlow after successful update
                refreshUserProfile()
                onSuccess()
                Log.d("ProfileViewModel", "User profile updated successfully in Firestore.")

            } catch (e: Exception) {
                val errorMessage = e.localizedMessage ?: "Failed to update profile."
                Log.e("ProfileViewModel", "Error updating profile: $errorMessage", e)
                onFailure(errorMessage)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}