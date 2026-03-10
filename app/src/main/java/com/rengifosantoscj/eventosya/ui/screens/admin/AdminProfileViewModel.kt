package com.rengifosantoscj.eventosya.ui.screens.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rengifosantoscj.eventosya.data.model.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AdminProfileViewModel : ViewModel() {
    var uiState by mutableStateOf(ProfileUiState())
        private set

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val doc = db.collection("users").document(uid).get().await()
                val user = doc.toObject(User::class.java)
                uiState = uiState.copy(user = user, isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, errorMessage = e.localizedMessage)
            }
        }
    }

    fun updateName(newName: String) {
        val currentUser = uiState.user ?: return
        uiState = uiState.copy(user = currentUser.copy(name = newName))
    }

    fun saveProfile() {
        val user = uiState.user ?: return
        if (user.name.isBlank()) {
            uiState = uiState.copy(errorMessage = "El nombre no puede estar vacío")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true, errorMessage = null, successMessage = null)
            try {
                db.collection("users").document(user.uid).set(user).await()
                uiState = uiState.copy(isSaving = false, successMessage = "Perfil actualizado correctamente")
            } catch (e: Exception) {
                uiState = uiState.copy(isSaving = false, errorMessage = e.localizedMessage)
            }
        }
    }

    fun clearMessages() {
        uiState = uiState.copy(errorMessage = null, successMessage = null)
    }
}
