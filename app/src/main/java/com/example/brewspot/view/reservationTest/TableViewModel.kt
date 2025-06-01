package com.example.brewspot.view.reservationTest

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.brewspot.view.home.Cafe
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await

class TableViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val _tables = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val tables: StateFlow<Map<String, Boolean>> = _tables

    var selectedTables: MutableSet<String> by mutableStateOf(mutableSetOf())

    var cafeId: String? by mutableStateOf(null)
    var userName: String? by mutableStateOf(null)
    var date: String? by mutableStateOf(null)
    var time: String? by mutableStateOf(null)
    var totalGuests: Int by mutableStateOf(0)
    // lastReservationDetails tidak lagi diperlukan dengan pendekatan reset unconditional
    // private var lastReservationDetails: Triple<String?, String?, Int>? = null

    private val _cafe = MutableStateFlow<Cafe?>(null)
    val cafe: StateFlow<Cafe?> = _cafe
    private var snapshotListener: ListenerRegistration? = null

    private val _resetTableStateTrigger = MutableSharedFlow<Unit>()
    val resetTableStateTrigger: SharedFlow<Unit> = _resetTableStateTrigger.asSharedFlow()

    fun triggerResetTableState() {
        viewModelScope.launch {
            _resetTableStateTrigger.emit(Unit)
        }
    }

    fun setReservationDetails(
        cafeId: String?,
        userName: String?,
        date: String?,
        time: String?,
        totalGuests: Int
    ) {
        // UNCONDITIONAL RESET AND REFRESH:
        // Setiap kali fungsi ini dipanggil, kita asumsikan ini adalah awal dari
        // proses pemilihan meja baru untuk sebuah reservasi.
        selectedTables = mutableSetOf() // <-- RESET TANPA SYARAT UNTUK PILIHAN LOKAL

        this.cafeId = cafeId
        this.userName = userName
        this.date = date
        this.time = time
        this.totalGuests = totalGuests

        cafeId?.let { id ->
            observeTables(id) // Refresh data meja dari Firestore
            viewModelScope.launch {
                try {
                    val documentSnapshot = db.collection("Cafe").document(id).get().await()
                    if (documentSnapshot.exists()) {
                        _cafe.value = Cafe.fromFirestore(documentSnapshot) // <-- PERUBAHAN DI SINI
                    } else {
                        _cafe.value = null // Atur ke null jika kafe tidak ditemukan
                    }
                } catch (e: Exception) {
                    println("Error fetching cafe details: ${e.message}")
                    _cafe.value = null // Atur ke null jika terjadi kesalahan
                }
            }
        }
    }


    private fun observeTables(currentCafeId: String) {
        snapshotListener?.remove()

        snapshotListener = db.collection("Cafe").document(currentCafeId).collection("Table")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    println("Firestore Error: ${error.message}")
                    return@addSnapshotListener
                }

                val tableMap = mutableMapOf<String, Boolean>()
                snapshots?.documents?.forEach { doc ->
                    val booked = doc.getBoolean("Book") ?: false
                    tableMap[doc.id] = booked
                }

                _tables.value = tableMap
            }
    }

    fun refreshTableStatusIfCafeIdSet() {
        cafeId?.let { id ->
            observeTables(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
    }

    fun toggleTableSelection(tableId: String) { // Removed 'maxSelections: Int' parameter
        if (_tables.value[tableId] == true) { //
            return //
        }

        val currentSelection = selectedTables.toMutableSet() //
        if (currentSelection.contains(tableId)) { //
            currentSelection.remove(tableId) //
        } else {
            // Removed the size check to allow unlimited selection
            currentSelection.add(tableId) //
        }
        selectedTables = currentSelection //
    }

    fun bookSelectedTables() {
        val currentCafeId = cafeId
        if (currentCafeId == null) {
            println("Error: cafeId is null, cannot book tables.")
            return
        }

        selectedTables.forEach { tableId ->
            db.collection("Cafe").document(currentCafeId)
                .collection("Table").document(tableId)
                .update("Book", true)
                .addOnSuccessListener {
                    println("Meja $tableId berhasil dibooking di cafe $currentCafeId!")
                }
                .addOnFailureListener { e ->
                    println("Error booking meja $tableId di cafe $currentCafeId: ${e.message}")
                }
        }
        selectedTables = mutableSetOf()
    }

    fun createReservation(
        cafeId: String,
        cafeName: String,
        userName: String,
        date: String,
        totalGuests: Int,
        time: String,
        onSuccess: (String) -> Unit,
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
            "selectedTables" to selectedTables.toList(),
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("reservations")
            .add(reservationData)
            .addOnSuccessListener { documentReference ->
                println("Reservation created successfully for $cafeName on $date at $time with tables: $selectedTables")
                onSuccess(documentReference.id)
            }
            .addOnFailureListener { e ->
                println("Error creating reservation: ${e.message}")
                onFailure(e.localizedMessage ?: "Failed to create reservation.")
            }
    }
}