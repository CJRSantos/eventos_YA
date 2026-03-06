package com.rengifosantoscj.eventosya.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user", // "user" o "admin"
    val highContrastEnabled: Boolean = false,
    val dynamicColorEnabled: Boolean = true
)
