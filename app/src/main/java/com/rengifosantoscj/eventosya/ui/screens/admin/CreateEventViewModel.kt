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
import java.util.UUID

data class CreateEventUiState(
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val location: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val category: String = "Música",
    val price: String = "0.0",
    val capacity: String = "50",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class CreateEventViewModel : ViewModel() {
    var uiState by mutableStateOf(CreateEventUiState())
        private set

    private val db = FirebaseFirestore.getInstance()

    fun onTitleChange(v: String) { uiState = uiState.copy(title = v) }
    fun onDescriptionChange(v: String) { uiState = uiState.copy(description = v) }
    fun onDateChange(v: String) { uiState = uiState.copy(date = v) }
    fun onLocationChange(v: String) { uiState = uiState.copy(location = v) }
    fun onLocationPicked(lat: Double, lng: Double) {
        uiState = uiState.copy(latitude = lat, longitude = lng)
    }
    fun onCategoryChange(v: String) { uiState = uiState.copy(category = v) }
    fun onPriceChange(v: String) { uiState = uiState.copy(price = v) }
    fun onCapacityChange(v: String) { uiState = uiState.copy(capacity = v) }

    fun saveEvent() {
        if (uiState.title.isBlank() || uiState.description.isBlank()) {
            uiState = uiState.copy(errorMessage = "Título y descripción son obligatorios")
            return
        }

        val event = Event(
            id = UUID.randomUUID().toString(),
            title = uiState.title,
            description = uiState.description,
            date = uiState.date,
            location = uiState.location,
            latitude = uiState.latitude,
            longitude = uiState.longitude,
            category = uiState.category,
            price = uiState.price.toDoubleOrNull() ?: 0.0,
            capacity = uiState.capacity.toIntOrNull() ?: 50
        )

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                db.collection("events").document(event.id).set(event).await()
                uiState = uiState.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, errorMessage = e.localizedMessage)
            }
        }
    }
    
    fun resetState() {
        uiState = CreateEventUiState()
    }
}
