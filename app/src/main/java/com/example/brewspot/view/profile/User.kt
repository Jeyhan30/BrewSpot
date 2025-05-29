package com.example.brewspot.view.profile


data class User(
    val username: String = "",
    val email: String = "",
    val image: String = "",
    val phoneNumber: String = ""// This will store the image URL or Base64 string
)