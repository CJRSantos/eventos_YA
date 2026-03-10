package com.rengifosantoscj.eventosya.data.model

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val location: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUrl: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val capacity: Int = 0,
    val registeredUsers: List<String> = emptyList()
)
