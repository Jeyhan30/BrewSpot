package com.example.brewspot.view.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewspot.view.home.Cafe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _recommendedCafes = MutableStateFlow<List<Cafe>>(emptyList())
    val recommendedCafes: StateFlow<List<Cafe>> = _recommendedCafes

    private val _popularCafes = MutableStateFlow<List<Cafe>>(emptyList())
    val popularCafes: StateFlow<List<Cafe>> = _popularCafes

    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchCafes()
    }

    private fun fetchCafes() {
        viewModelScope.launch {
            try {
                // Fetch recommended cafes
                firestore.collection("Cafe")
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val cafes = querySnapshot.documents.map { Cafe.fromFirestore(it) }
                        _recommendedCafes.value = cafes
                        // For simplicity, populate popular cafes with the same data initially
                        // You might add specific queries or sorting for popular cafes later
                        _popularCafes.value = cafes.shuffled() // Shuffle for different order in popular
                    }
                    .addOnFailureListener { e ->
                        // Handle error, e.g., log it or show a Toast
                        println("Error fetching cafes: $e")
                    }

            } catch (e: Exception) {
                println("Exception in fetchCafes: $e")
            }
        }
    }
}