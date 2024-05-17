package com.example.mapssages.model

import java.util.Date

class Message(
    var id: String? = "",
    val firstName: String? = "",
    val lastName: String? = "",
    val latitude: Float? = 0f,
    val longitude: Float? = 0f,
    val message: String? = "",
    val date: Date? = null
)
