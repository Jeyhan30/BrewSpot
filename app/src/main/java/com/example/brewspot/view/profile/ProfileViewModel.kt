package com.example.brewspot.view.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewspot.view.profile.User // Pastikan Anda mengimpor kelas User dari package yang benar
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
            // Kompres gambar (sesuaikan format dan kualitas jika perlu)
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

    // Fungsi updateProfile sekarang menerima `base64Image` bukan `imageUrl`
    fun updateProfile(
        username: String,
        phoneNumber: String,
        base64Image: String? = null, // Ganti imageUrl menjadi base64Image
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
                    userUpdates["image"] = base64Image // Simpan Base64 di field imageUrl
                }

                firestore.collection("users").document(userId)
                    .update(userUpdates)
                    .await()

                // Perbarui state _currentUser di ViewModel setelah update berhasil
                _currentUser.value = _currentUser.value?.copy(
                    username = username,
                    phoneNumber = phoneNumber,
                    image = base64Image ?: _currentUser.value?.image ?: "" // Perbarui imageUrl dengan Base64
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

        // Re-authenticate user with their old password
        // This is a crucial security step before allowing password changes
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
        // The addAuthStateListener in init block will handle clearing _currentUser.value
        Log.d("ProfileViewModel", "User signed out from Firebase.")
    }
}