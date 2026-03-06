package com.rengifosantoscj.eventosya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.rengifosantoscj.eventosya.data.preferences.ThemePreferences
import com.rengifosantoscj.eventosya.ui.screens.admin.AdminNavigationContainer
import com.rengifosantoscj.eventosya.ui.screens.login.LoginScreen
import com.rengifosantoscj.eventosya.ui.screens.login.LoginViewModel
import com.rengifosantoscj.eventosya.ui.screens.register.RegisterScreen
import com.rengifosantoscj.eventosya.ui.screens.register.RegisterViewModel
import com.rengifosantoscj.eventosya.ui.theme.EventosYATheme

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object Admin : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val preferences = remember { ThemePreferences(context) }
            val dynamicColorEnabled by preferences.dynamicColorEnabled.collectAsState(initial = true)
            val highContrastEnabled by preferences.highContrastEnabled.collectAsState(initial = false)
            
            val darkTheme = isSystemInDarkTheme() || highContrastEnabled

            val loginViewModel: LoginViewModel = viewModel()
            val registerViewModel: RegisterViewModel = viewModel()

            var currentScreen by remember { 
                mutableStateOf<Screen>(
                    if (FirebaseAuth.getInstance().currentUser != null) Screen.Admin else Screen.Login
                ) 
            }

            EventosYATheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicColorEnabled,
                highContrast = highContrastEnabled // Pasamos el alto contraste al tema
            ) {
                Crossfade(
                    targetState = currentScreen,
                    label = "MainTransition",
                    modifier = Modifier.fillMaxSize()
                ) { screen ->
                    when (screen) {
                        Screen.Login -> {
                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginSuccess = { currentScreen = Screen.Admin },
                                onNavigateToRegister = { currentScreen = Screen.Register }
                            )
                        }
                        Screen.Register -> {
                            RegisterScreen(
                                viewModel = registerViewModel,
                                onNavigateBack = { currentScreen = Screen.Login },
                                onRegisterSuccess = { 
                                    loginViewModel.resetLoginState()
                                    currentScreen = Screen.Admin 
                                }
                            )
                        }
                        Screen.Admin -> {
                            AdminNavigationContainer(
                                onLogout = {
                                    FirebaseAuth.getInstance().signOut()
                                    loginViewModel.resetLoginState()
                                    currentScreen = Screen.Login
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
