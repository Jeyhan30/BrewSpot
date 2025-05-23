package com.example.brewspot.view.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewspot.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        repository.loginWithEmail(email, password, onResult)
    }

    fun loginWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        repository.loginWithGoogle(idToken, onResult)
    }
}
