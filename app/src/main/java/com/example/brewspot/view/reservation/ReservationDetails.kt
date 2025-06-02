package com.example.brewspot.view.reservation

data class ReservationDetails(
    val cafeId: String = "",
    val cafeName: String = "",
    val userName: String = "",
    val date: String = "",
    val totalGuests: Int = 0,
    val time: String = ""
)