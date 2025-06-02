package com.example.brewspot.view.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class ProfileViewModel : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val _isLoading = MutableStateFlow(false)

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

    fun convertUriToBase64(context: Context, image: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(image)
            val imageBitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            println("Error converting Uri to Base64: ${e.message}")
            null
        }
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
                            phoneNumber = "",
                            image = ""
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


    fun updateProfile(
        username: String,
        phoneNumber: String,
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
                val updates = mutableMapOf<String, Any>(
                    "username" to username
                )
                updates["phoneNumber"] = phoneNumber

                firestore.collection("users").document(firebaseUser.uid)
                    .update(updates)
                    .await()

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

    fun updateProfile(
        username: String,
        phoneNumber: String,
        base64Image: String? = null,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        _isLoading.value = true
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onFailure("User not logged in.")
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            try {
                val userUpdates = mutableMapOf<String, Any>(
                    "username" to username,
                    "phoneNumber" to phoneNumber
                )
                if (base64Image != null) {
                    userUpdates["image"] = base64Image
                }

                firestore.collection("users").document(userId)
                    .update(userUpdates)
                    .await()

                _currentUser.value = _currentUser.value?.copy(
                    username = username,
                    phoneNumber = phoneNumber,
                    image = base64Image ?: _currentUser.value?.image ?: ""
                )
                onSuccess()
            } catch (e: Exception) {
                onFailure("Gagal memperbarui profil: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun changePassword(
        oldPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            onFailure("User not logged in.")
            return
        }

        val credential = EmailAuthProvider.getCredential(firebaseUser.email ?: "", oldPassword)

        viewModelScope.launch {
            try {
                firebaseUser.reauthenticate(credential).await()
                Log.d("ProfileViewModel", "User re-authenticated successfully.")

                // Update password
                firebaseUser.updatePassword(newPassword).await()
                Log.d("ProfileViewModel", "Password updated successfully.")
                onSuccess()

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Kata sandi lama salah."
                    is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException -> "Sesi Anda telah berakhir, harap masuk kembali."
                    else -> e.localizedMessage ?: "Gagal mengganti kata sandi."
                }
                Log.e("ProfileViewModel", "Error changing password: $errorMessage", e)
                onFailure(errorMessage)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
    fun logout() {
        auth.signOut()
        Log.d("ProfileViewModel", "User signed out from Firebase.")
    }
}