package com.rengifosantoscj.eventosya.ui.screens.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.rengifosantoscj.eventosya.data.model.Event
import com.rengifosantoscj.eventosya.data.model.Ticket
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

sealed class EventDetailsUiState {
    object Loading : EventDetailsUiState()
    data class Success(
        val event: Event, 
        val isAdmin: Boolean, 
        val isRegistered: Boolean,
        val registeredUserEmails: List<String> = emptyList(),
        val ticketId: String? = null
    ) : EventDetailsUiState()
    data class Error(val message: String) : EventDetailsUiState()
}

class EventDetailsViewModel : ViewModel() {
    var uiState by mutableStateOf<EventDetailsUiState>(EventDetailsUiState.Loading)
        private set

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun loadEventDetails(eventId: String) {
        viewModelScope.launch {
            uiState = EventDetailsUiState.Loading
            try {
                val user = auth.currentUser
                val userDoc = db.collection("users").document(user?.uid ?: "").get().await()
                val isAdmin = userDoc.getString("role") == "admin"
                
                val eventDoc = db.collection("events").document(eventId).get().await()
                val event = eventDoc.toObject(Event::class.java)

                if (event != null) {
                    val isRegistered = event.registeredUsers.contains(user?.uid)
                    
                    var emails = emptyList<String>()
                    var myTicketId: String? = null

                    if (isAdmin && event.registeredUsers.isNotEmpty()) {
                        val usersSnapshot = db.collection("users")
                            .whereIn("uid", event.registeredUsers)
                            .get()
                            .await()
                        emails = usersSnapshot.documents.mapNotNull { it.getString("email") }
                    }

                    if (!isAdmin && isRegistered) {
                        val ticketDoc = db.collection("tickets")
                            .whereEqualTo("eventId", eventId)
                            .whereEqualTo("userId", user?.uid)
                            .get()
                            .await()
                        myTicketId = ticketDoc.documents.firstOrNull()?.id
                    }

                    uiState = EventDetailsUiState.Success(event, isAdmin, isRegistered, emails, myTicketId)
                } else {
                    uiState = EventDetailsUiState.Error("Evento no encontrado")
                }
            } catch (e: Exception) {
                uiState = EventDetailsUiState.Error(e.localizedMessage ?: "Error al cargar detalles")
            }
        }
    }

    fun toggleRegistration(eventId: String) {
        val currentState = uiState as? EventDetailsUiState.Success ?: return
        val user = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val eventRef = db.collection("events").document(eventId)
                if (currentState.isRegistered) {
                    // Cancelar: Borrar ticket y quitar de lista
                    eventRef.update("registeredUsers", FieldValue.arrayRemove(user.uid)).await()
                    val tickets = db.collection("tickets")
                        .whereEqualTo("eventId", eventId)
                        .whereEqualTo("userId", user.uid)
                        .get().await()
                    tickets.documents.forEach { it.reference.delete().await() }
                } else {
                    // Inscribir: Crear ticket y añadir a lista
                    val ticketId = UUID.randomUUID().toString()
                    val newTicket = Ticket(
                        id = ticketId,
                        eventId = eventId,
                        eventTitle = currentState.event.title,
                        userId = user.uid,
                        userEmail = user.email ?: "",
                        date = currentState.event.date,
                        location = currentState.event.location,
                        qrCode = ticketId
                    )
                    db.collection("tickets").document(ticketId).set(newTicket).await()
                    eventRef.update("registeredUsers", FieldValue.arrayUnion(user.uid)).await()
                }
                loadEventDetails(eventId)
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}
