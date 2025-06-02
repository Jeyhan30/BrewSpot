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

    private val _searchResults = MutableStateFlow<List<Cafe>>(emptyList())
    val searchResults: StateFlow<List<Cafe>> = _searchResults

    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchCafes()
        setupRealtimeCafeListeners()
        viewModelScope.launch {
            _searchQuery.combine(_recommendedCafes) { query, cafes ->
                if (query.isBlank()) {
                    emptyList()
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
                        _popularCafes.value = cafes.shuffled()
                    }
                    .addOnFailureListener { e ->
                        println("Error fetching cafes: $e")
                    }

            } catch (e: Exception) {
                println("Exception in fetchCafes: $e")
            }
        }
    }

    private fun setupRealtimeCafeListeners() {
        firestore.collection("Cafe")
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

        firestore.collection("Cafe")
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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}