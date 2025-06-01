package com.example.brewspot.view.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewspot.view.home.Cafe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _recommendedCafes = MutableStateFlow<List<Cafe>>(emptyList())
    val recommendedCafes: StateFlow<List<Cafe>> = _recommendedCafes

    private val _popularCafes = MutableStateFlow<List<Cafe>>(emptyList())
    val popularCafes: StateFlow<List<Cafe>> = _popularCafes

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // New StateFlow for filtered search results
    private val _searchResults = MutableStateFlow<List<Cafe>>(emptyList())
    val searchResults: StateFlow<List<Cafe>> = _searchResults

    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchCafes() // Your existing one-time fetch
        setupRealtimeCafeListeners() // New function call for real-time updates
        // Combine the search query with the recommended cafes to filter results
        viewModelScope.launch {
            _searchQuery.combine(_recommendedCafes) { query, cafes ->
                if (query.isBlank()) {
                    emptyList() // If search query is empty, show no search results
                } else {
                    cafes.filter { cafe ->
                        cafe.name.contains(query, ignoreCase = true)
                    }
                }
            }.collect { filteredList ->
                _searchResults.value = filteredList
            }
        }
    }

    private fun fetchCafes() {
        viewModelScope.launch {
            try {
                firestore.collection("Cafe")
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val cafes = querySnapshot.documents.map { Cafe.fromFirestore(it) }
                        _recommendedCafes.value = cafes
                        // For simplicity, populate popular cafes with the same data initially
                        _popularCafes.value = cafes.shuffled() // Shuffle for different order in popular
                    }
                    .addOnFailureListener { e ->
                        println("Error fetching cafes: $e")
                    }

            } catch (e: Exception) {
                println("Exception in fetchCafes: $e")
            }
        }
    }

    // New function to set up real-time Firestore listeners
    private fun setupRealtimeCafeListeners() {
        // Listener for Recommended Cafes
        firestore.collection("Cafe")
            // You might want to add specific ordering or filtering here for 'recommended'
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Listen failed for recommended cafes: $e")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val cafes = snapshot.documents.map { Cafe.fromFirestore(it) }
                    _recommendedCafes.value = cafes
                } else {
                    println("Current data for recommended cafes: null")
                    _recommendedCafes.value = emptyList()
                }
            }

        // Listener for Popular Cafes
        firestore.collection("Cafe")
            // Example: Order by a 'popularityScore' field or number of reviews for 'popular'
            // .orderBy("popularityScore", com.google.firebase.firestore.Query.Direction.DESCENDING)
            // .limit(10) // Limit to top 10 popular cafes
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Listen failed for popular cafes: $e")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val cafes = snapshot.documents.map { Cafe.fromFirestore(it) }
                    _popularCafes.value = cafes
                } else {
                    println("Current data for popular cafes: null")
                    _popularCafes.value = emptyList()
                }
            }
    }

    // Function to update the search query
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}