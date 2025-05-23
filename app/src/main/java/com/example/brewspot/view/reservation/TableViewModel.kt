package com.example.brewspot.view.reservation

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TableViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val _tables = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val tables: StateFlow<Map<String, Boolean>> = _tables

    var selectedTable: String? = null

    init {
        observeTables()
    }

    private fun observeTables() {
        db.collection("Cafe").document("Jokopi").collection("Table")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    println("Firestore Error: ${error.message}")
                    return@addSnapshotListener
                }

                val tableMap = mutableMapOf<String, Boolean>()
                snapshots?.documents?.forEach { doc ->
                    val booked = doc.getBoolean("Book") ?: false
                    println("Table: ${doc.id}, Booked: $booked") // ⬅️ Debug print
                    tableMap[doc.id] = booked
                }

                _tables.value = tableMap
            }
    }


    fun bookSelectedTable() {
        selectedTable?.let {
            db.collection("Cafe").document("Jokopi")
                .collection("Table").document(it)
                .update("Book", true)
        }
    }
}
