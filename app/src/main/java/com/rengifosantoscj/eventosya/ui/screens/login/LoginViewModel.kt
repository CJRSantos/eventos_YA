package com.rengifosantoscj.eventosya.ui.screens.login

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.rengifosantoscj.eventosya.data.model.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isLoginSuccess: Boolean = false,
    val userRole: String? = null
)

class LoginViewModel : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email, emailError = null, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password, passwordError = null, errorMessage = null)
    }
    
    fun resetLoginState() {
        uiState = uiState.copy(isLoginSuccess = false, isLoading = false, errorMessage = null, password = "")
    }

    fun loginWithEmail() {
        val email = uiState.email.trim()
        val password = uiState.password
        
        var hasError = false
        var eErr: String? = null
        var pErr: String? = null

        if (email.isBlank()) {
            eErr = "El correo no puede estar vacío"
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            eErr = "Formato de correo inválido"
            hasError = true
        }
        
        if (password.isBlank()) {
            pErr = "La contraseña no puede estar vacía"
            hasError = true
        }

        if (hasError) {
            uiState = uiState.copy(emailError = eErr, passwordError = pErr)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                uiState = uiState.copy(isLoading = false, isLoginSuccess = true)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false, 
                    errorMessage = e.localizedMessage ?: "Error al iniciar sesión"
                )
            }
        }
    }

    fun sendPasswordReset() {
        val email = uiState.email.trim()
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            uiState = uiState.copy(emailError = "Ingresa un correo válido para recuperar tu contraseña")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null, infoMessage = null)
            try {
                auth.sendPasswordResetEmail(email).await()
                uiState = uiState.copy(
                    isLoading = false, 
                    infoMessage = "Se ha enviado un correo para restablecer tu contraseña"
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al enviar correo de recuperación"
                )
            }
        }
    }

    fun handleGoogleSignInResult(credential: Credential) {
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(firebaseCredential).await()
                val firebaseUser = result.user

                if (firebaseUser != null) {
                    val userDoc = db.collection("users").document(firebaseUser.uid).get().await()
                    if (!userDoc.exists()) {
                        val newUser = User(
                            uid = firebaseUser.uid,
                            name = firebaseUser.displayName ?: "Usuario Google",
                            email = firebaseUser.email ?: "",
                            role = "user"
                        )
                        db.collection("users").document(firebaseUser.uid).set(newUser).await()
                    }
                }

                uiState = uiState.copy(isLoading = false, isLoginSuccess = true)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, errorMessage = e.localizedMessage)
            }
        }
    }

    fun onGoogleSignInError(error: String) { uiState = uiState.copy(errorMessage = error) }
    fun clearInfoMessage() { uiState = uiState.copy(infoMessage = null) }
}
