package com.rengifosantoscj.eventosya.ui.screens.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.rengifosantoscj.eventosya.data.model.Event
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AdminHomeUiState {
    object Loading : AdminHomeUiState()
    data class Success(val events: List<Event>) : AdminHomeUiState()
    data class Error(val message: String) : AdminHomeUiState()
    object Empty : AdminHomeUiState()
}

class AdminHomeViewModel : ViewModel() {
    var uiState by mutableStateOf<AdminHomeUiState>(AdminHomeUiState.Loading)
        private set

    private val db = FirebaseFirestore.getInstance()

    init {
        fetchEvents()
    }

    fun fetchEvents() {
        viewModelScope.launch {
            uiState = AdminHomeUiState.Loading
            try {
                val snapshot = db.collection("events").get().await()
                val events = snapshot.toObjects(Event::class.java)
                
                uiState = if (events.isEmpty()) {
                    AdminHomeUiState.Empty
                } else {
                    AdminHomeUiState.Success(events)
                }
            } catch (e: Exception) {
                uiState = AdminHomeUiState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            try {
                db.collection("events").document(eventId).delete().await()
                fetchEvents() // Recargar lista
            } catch (e: Exception) {
                // Manejar error de borrado
            }
        }
    }
}
