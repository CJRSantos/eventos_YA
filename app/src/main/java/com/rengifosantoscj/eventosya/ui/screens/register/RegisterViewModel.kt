package com.rengifosantoscj.eventosya.ui.screens.register

import android.util.Patterns
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

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegisterSuccess: Boolean = false
)

class RegisterViewModel : ViewModel() {

    var uiState by mutableStateOf(RegisterUiState())
        private set

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun onNameChange(name: String) {
        uiState = uiState.copy(name = name, nameError = null)
    }

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email, emailError = null)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password, passwordError = null)
    }

    fun onConfirmPasswordChange(password: String) {
        uiState = uiState.copy(confirmPassword = password, confirmPasswordError = null)
    }

    fun register() {
        val name = uiState.name.trim()
        val email = uiState.email.trim()
        val password = uiState.password
        val confirmPassword = uiState.confirmPassword

        var hasError = false
        var nErr: String? = null
        var eErr: String? = null
        var pErr: String? = null
        var cpErr: String? = null

        if (name.isBlank()) {
            nErr = "El nombre es obligatorio"
            hasError = true
        }
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            eErr = "Correo inválido"
            hasError = true
        }
        if (password.length < 6) {
            pErr = "Mínimo 6 caracteres"
            hasError = true
        }
        if (password != confirmPassword) {
            cpErr = "Las contraseñas no coinciden"
            hasError = true
        }

        if (hasError) {
            uiState = uiState.copy(
                nameError = nErr,
                emailError = eErr,
                passwordError = pErr,
                confirmPasswordError = cpErr
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("Error al obtener UID")
                
                val newUser = User(
                    uid = uid,
                    name = name,
                    email = email,
                    role = "user"
                )
                
                db.collection("users").document(uid).set(newUser).await()
                
                uiState = uiState.copy(isLoading = false, isRegisterSuccess = true)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al registrarse"
                )
            }
        }
    }
}
