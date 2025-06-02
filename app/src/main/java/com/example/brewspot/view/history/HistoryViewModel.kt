package com.example.brewspot.view.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewspot.view.home.Cafe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class HistoryViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _bookingHistory = MutableStateFlow<List<BookingHistory>>(emptyList())
    val bookingHistory: StateFlow<List<BookingHistory>> = _bookingHistory

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedBooking = MutableStateFlow<BookingHistory?>(null)
    val selectedBooking: StateFlow<BookingHistory?> = _selectedBooking

    private val _internalBookingList = mutableListOf<BookingHistory>()
    private val authStateListener: FirebaseAuth.AuthStateListener

    init {
        Log.d("HistoryViewModel", "Initializing HistoryViewModel...")
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.d("HistoryViewModel", "Auth state changed: User is logged in (${user.uid}). Fetching history...")
                fetchBookingHistory()
            } else {
                Log.d("HistoryViewModel", "Auth state changed: No user logged in. Clearing history...")
                _internalBookingList.clear()
                _bookingHistory.value = emptyList()
                _selectedBooking.value = null
            }
        }
        auth.addAuthStateListener(authStateListener)
    }

    fun fetchBookingHistory() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Log.w("HistoryViewModel", "fetchBookingHistory: User not logged in, cannot fetch history. userId is null.")
            _bookingHistory.value = emptyList()
            _internalBookingList.clear()
            return
        }

        Log.d("HistoryViewModel", "Fetching history for authenticated userId: $userId")

        firestore.collection("history")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("HistoryViewModel", "Error fetching booking history: ${error.message}", error)
                    _bookingHistory.value = emptyList()
                    _internalBookingList.clear()
                    return@addSnapshotListener
                }

                if (snapshots == null) {
                    Log.d("HistoryViewModel", "Received null snapshots, possibly no data or initial state.")
                    _bookingHistory.value = emptyList()
                    _internalBookingList.clear()
                    return@addSnapshotListener
                }

                viewModelScope.launch {
                    Log.d("HistoryViewModel", "Received ${snapshots.documentChanges.size} document changes for userId: $userId.")
                    Log.d("HistoryViewModel", "IsFromCache: ${snapshots.metadata.isFromCache}")

                    val currentList = _internalBookingList.toMutableList()
                    Log.d("HistoryViewModel", "Initial currentList size: ${currentList.size}")

                    Log.d("HistoryViewModel", "Number of documents in snapshot: ${snapshots.documents.size}")
                    if (snapshots.documents.isEmpty()) {
                        Log.d("HistoryViewModel", "Snapshot is empty for userId: $userId. Check Firestore data and userId match.")
                    }

                    for (dc in snapshots.documentChanges) {
                        val source = if (snapshots.metadata.isFromCache) "local cache" else "server"
                        Log.d("HistoryViewModel", "Processing Change Type: ${dc.type}, Document ID: ${dc.document.id}, Old Index: ${dc.oldIndex}, New Index: ${dc.newIndex}, Source: $source")

                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                try {
                                    var booking = BookingHistory.fromFirestore(dc.document)
                                    Log.d("HistoryViewModel", "ADDED document's userId: ${dc.document.getString("userId")}, Expected: $userId")

                                    val cafeDoc = firestore.collection("Cafe").document(booking.cafeId).get().await()
                                    if (cafeDoc.exists()) {
                                        val cafe = Cafe.fromFirestore(cafeDoc)
                                        booking = booking.copy(cafeImageUrl = cafe.image)
                                        Log.d("HistoryViewModel", "Fetched cafe image for ADDED booking ${booking.id}")
                                    } else {
                                        Log.w("HistoryViewModel", "Cafe details not found for ADDED booking ${booking.id} with cafeId ${booking.cafeId}")
                                    }

                                    val existingIndex = currentList.indexOfFirst { it.id == booking.id }
                                    if (existingIndex != -1) {
                                        currentList[existingIndex] = updateBookingStatus(booking)
                                        Log.d("HistoryViewModel", "Updated existing item (ADDED type) ${booking.id}")
                                    } else {
                                        val targetIndex = if (dc.newIndex >= 0 && dc.newIndex <= currentList.size) dc.newIndex else currentList.size
                                        currentList.add(targetIndex, updateBookingStatus(booking))
                                        Log.d("HistoryViewModel", "Added new item ${booking.id} at index ${targetIndex}. Name: ${booking.cafeName}")
                                    }
                                } catch (e: Exception) {
                                    Log.e("HistoryViewModel", "Error processing ADDED document ${dc.document.id}: ${e.message}", e)
                                }
                            }
                            DocumentChange.Type.MODIFIED -> {
                                try {
                                    var booking = BookingHistory.fromFirestore(dc.document)
                                    Log.d("HistoryViewModel", "MODIFIED document's userId: ${dc.document.getString("userId")}, Expected: $userId")

                                    val cafeDoc = firestore.collection("Cafe").document(booking.cafeId).get().await()
                                    if (cafeDoc.exists()) {
                                        val cafe = Cafe.fromFirestore(cafeDoc)
                                        booking = booking.copy(cafeImageUrl = cafe.image)
                                        Log.d("HistoryViewModel", "Fetched cafe image for MODIFIED booking ${booking.id}")
                                    } else {
                                        Log.w("HistoryViewModel", "Cafe details not found for MODIFIED booking ${booking.id} with cafeId ${booking.cafeId}")
                                    }

                                    val oldIndex = currentList.indexOfFirst { it.id == booking.id }
                                    if (oldIndex != -1) {
                                        currentList.removeAt(oldIndex)
                                        val targetIndex = if (dc.newIndex >= 0 && dc.newIndex <= currentList.size) dc.newIndex else currentList.size
                                        currentList.add(targetIndex, updateBookingStatus(booking))
                                        Log.d("HistoryViewModel", "Modified and moved item ${booking.id} from $oldIndex to $targetIndex. Name: ${booking.cafeName}")
                                    } else {
                                        val targetIndex = if (dc.newIndex >= 0 && dc.newIndex <= currentList.size) currentList.size else dc.newIndex
                                        currentList.add(targetIndex, updateBookingStatus(booking))
                                        Log.w("HistoryViewModel", "Modified item ${booking.id} not found locally at old index, adding it at ${targetIndex}. Name: ${booking.cafeName}")
                                    }
                                } catch (e: Exception) {
                                    Log.e("HistoryViewModel", "Error processing MODIFIED document ${dc.document.id}: ${e.message}", e)
                                }
                            }
                            DocumentChange.Type.REMOVED -> {
                                val removedId = dc.document.id
                                Log.d("HistoryViewModel", "REMOVED event detected for Document ID: $removedId")
                                Log.d("HistoryViewModel", "Current list items BEFORE removal attempt (size: ${currentList.size}):")
                                currentList.forEach { item ->
                                    Log.d("HistoryViewModel", "  -> Existing item ID: ${item.id}, Cafe Name: ${item.cafeName}")
                                }

                                val initialSize = currentList.size
                                currentList.removeAll { it.id == removedId }

                                if (currentList.size < initialSize) {
                                    Log.d("HistoryViewModel", "SUCCESS - Document with ID: $removedId was removed from local list.")
                                } else {
                                    Log.e("HistoryViewModel", "FAILURE - Document with ID: $removedId was NOT found in local list to remove. This means the ID from Firestore did not match any ID in _internalBookingList.")
                                }
                                Log.d("HistoryViewModel", "Current list items AFTER removal attempt (size: ${currentList.size}):")
                                currentList.forEach { item ->
                                    Log.d("HistoryViewModel", "  -> Remaining item ID: ${item.id}, Cafe Name: ${item.cafeName}")
                                }
                            }
                        }
                    }
                    _internalBookingList.clear()
                    _internalBookingList.addAll(currentList.sortedByDescending { it.timestamp })
                    _bookingHistory.value = _internalBookingList.toList()
                    Log.d("HistoryViewModel", "Final booking history updated. New size: ${_bookingHistory.value.size}")
                }
            }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        Log.d("HistoryViewModel", "Search query updated to: $query")
    }

    fun onBookingSelected(booking: BookingHistory) {
        _selectedBooking.value = booking
    }

    fun dismissBookingDetail() {
        _selectedBooking.value = null
    }

    private fun updateBookingStatus(booking: BookingHistory): BookingHistory {
        if (booking.status == "Dibatalkan" || booking.status == "Kadaluarsa") {
            return booking
        }

        val currentTime = Calendar.getInstance().time
        val bookingDateTimeString = "${booking.date} ${booking.time}"
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        return try {
            val bookingDate = sdf.parse(bookingDateTimeString)
            if (bookingDate != null && bookingDate.before(currentTime)) {
                val diffInMillis = currentTime.time - bookingDate.time
                val hoursPassed = TimeUnit.MILLISECONDS.toHours(diffInMillis)

                if (hoursPassed >= 2 && booking.status == "Sudah Dibayar") {
                    Log.d("HistoryViewModel", "Booking ${booking.id} status changed to 'Kadaluarsa'.")
                    booking.copy(status = "Kadaluarsa")
                } else {
                    booking
                }
            } else {
                booking
            }
        } catch (e: Exception) {
            Log.e("HistoryViewModel", "Error parsing date/time for booking status: ${e.message}", e)
            booking
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        Log.d("HistoryViewModel", "ViewModel cleared. AuthStateListener removed.")
    }
}