package com.rengifosantoscj.eventosya.data.model

data class Ticket(
    val id: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val date: String = "",
    val location: String = "",
    val qrCode: String = "", // Contenido del QR (puede ser el ticketId)
    val status: String = "active" // "active", "used", "cancelled"
)
