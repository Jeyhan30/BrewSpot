package com.example.brewspot.view.reservationTest

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.brewspot.view.home.Cafe // Import Cafe model if you want to store cafe details
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

class TableViewModel : ViewModel() {
    private val db = Firebase.firestore // Correctly initialized as 'db'
    private val auth = FirebaseAuth.getInstance() // Initialize FirebaseAuth
    private val _tables = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val tables: StateFlow<Map<String, Boolean>> = _tables

    // Use a MutableSet for selected tables
    var selectedTables: MutableSet<String> by mutableStateOf(mutableSetOf())

    // Store reservation details
    // cafeId sudah ada di sini, kita akan menggunakannya
    var cafeId: String? by mutableStateOf(null) // Pastikan ini tetap ada
    var userName: String? by mutableStateOf(null)
    var date: String? by mutableStateOf(null)
    var time: String? by mutableStateOf(null)
    var totalGuests: Int by mutableStateOf(0)

    // You might want to fetch cafe details here to get the name for reservation
    var cafe: Cafe? by mutableStateOf(null)

    // Inisialisasi tidak lagi memanggil observeTables() secara langsung
    // karena cafeId belum tersedia saat init.
    // observeTables() akan dipanggil setelah cafeId diset via setReservationDetails.

    // New function to set reservation details from CafeDetailScreen
    fun setReservationDetails(
        cafeId: String?,
        userName: String?,
        date: String?,
        time: String?,
        totalGuests: Int
    ) {
        // Hanya perbarui jika cafeId berubah atau jika ini adalah inisialisasi pertama
        if (this.cafeId != cafeId) {
            this.cafeId = cafeId
            // Panggil observeTables hanya jika cafeId valid
            cafeId?.let { id ->
                observeTables(id) // Panggil observeTables dengan cafeId
                viewModelScope.launch {
                    try {
                        val documentSnapshot = db.collection("Cafe").document(id).get().await()
                        if (documentSnapshot.exists()) {
                            cafe = Cafe.fromFirestore(documentSnapshot)
                        }
                    } catch (e: Exception) {
                        println("Error fetching cafe details: ${e.message}")
                    }
                }
            }
        }
        this.userName = userName
        this.date = date
        this.time = time
        this.totalGuests = totalGuests
    }


    // Fungsi observeTables sekarang menerima cafeId
    private fun observeTables(currentCafeId: String) {
        db.collection("Cafe").document(currentCafeId).collection("Table") // Gunakan currentCafeId di sini
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

    fun toggleTableSelection(tableId: String, maxSelections: Int) {
        // Ensure the table is not already booked
        if (_tables.value[tableId] == true) {
            return // Cannot select a booked table
        }

        val currentSelection = selectedTables.toMutableSet()
        if (currentSelection.contains(tableId)) {
            currentSelection.remove(tableId)
        } else {
            if (currentSelection.size < maxSelections) { // Only add if not exceeding max
                currentSelection.add(tableId)
            } else {
                // Optionally, inform the user they cannot select more tables
                println("Cannot select more than $maxSelections tables.")
            }
        }
        selectedTables = currentSelection
    }

    // Function to book all selected tables
    fun bookSelectedTables() {
        val currentCafeId = cafeId // Ambil cafeId dari state ViewModel
        if (currentCafeId == null) {
            println("Error: cafeId is null, cannot book tables.")
            return
        }

        selectedTables.forEach { tableId ->
            db.collection("Cafe").document(currentCafeId) // Gunakan currentCafeId di sini
                .collection("Table").document(tableId)
                .update("Book", true)
                .addOnSuccessListener {
                    println("Meja $tableId berhasil dibooking di cafe $currentCafeId!")
                }
                .addOnFailureListener { e ->
                    println("Error booking meja $tableId di cafe $currentCafeId: ${e.message}")
                }
        }
        // Clear selected tables after booking
        selectedTables = mutableSetOf()
    }

    // New function to create the reservation document
    fun createReservation(
        cafeId: String, // Parameter cafeId sudah ada, pastikan ini yang digunakan
        cafeName: String,
        userName: String,
        date: String,
        totalGuests: Int,
        time: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val reservationData = hashMapOf(
            "cafeId" to cafeId, // Gunakan cafeId yang diterima
            "cafeName" to cafeName,
            "userId" to auth.currentUser?.uid,
            "userName" to userName,
            "date" to date,
            "totalGuests" to totalGuests,
            "time" to time,
            "selectedTables" to selectedTables.toList(), // Save the list of selected table IDs
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("reservations") // Ubah 'firestore' menjadi 'db' di sini
            .add(reservationData)
            .addOnSuccessListener {
                println("Reservation created successfully for $cafeName on $date at $time with tables: $selectedTables")
                onSuccess()
            }
            .addOnFailureListener { e ->
                println("Error creating reservation: ${e.message}")
                onFailure(e.localizedMessage ?: "Failed to create reservation.")
            }
    }
}