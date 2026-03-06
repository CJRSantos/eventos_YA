package com.rengifosantoscj.eventosya.ui.screens.admin

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

sealed class AdminScreen(val route: String, val label: String, val icon: ImageVector) {
    object Events : AdminScreen("admin_events", "Eventos", Icons.Default.Event)
    object CreateEvent : AdminScreen("create_event", "Nuevo Evento", Icons.Default.Event) // Pantalla interna
    object Tickets : AdminScreen("admin_tickets", "Tickets", Icons.Default.ConfirmationNumber)
    object Profile : AdminScreen("admin_profile", "Perfil", Icons.Default.Person)
}

@Composable
fun AdminNavigationContainer(
    onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf<AdminScreen>(AdminScreen.Events) }
    val screens = listOf(AdminScreen.Events, AdminScreen.Tickets, AdminScreen.Profile)

    BoxWithConstraints {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            bottomBar = {
                if (!useRail && currentScreen != AdminScreen.CreateEvent) {
                    NavigationBar {
                        screens.forEach { screen ->
                            NavigationBarItem(
                                selected = currentScreen == screen,
                                onClick = { currentScreen = screen },
                                label = { Text(screen.label) },
                                icon = { Icon(screen.icon, contentDescription = screen.label) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (useRail && currentScreen != AdminScreen.CreateEvent) {
                    NavigationRail {
                        screens.forEach { screen ->
                            NavigationRailItem(
                                selected = currentScreen == screen,
                                onClick = { currentScreen = screen },
                                label = { Text(screen.label) },
                                icon = { Icon(screen.icon, contentDescription = screen.label) }
                            )
                        }
                    }
                }

                Crossfade(targetState = currentScreen, label = "AdminNavTransition") { screen ->
                    when (screen) {
                        AdminScreen.Events -> {
                            AdminHomeScreen(
                                onNavigateToCreate = { currentScreen = AdminScreen.CreateEvent }
                            )
                        }
                        AdminScreen.CreateEvent -> {
                            CreateEventScreen(
                                onNavigateBack = { currentScreen = AdminScreen.Events }
                            )
                        }
                        AdminScreen.Tickets -> AdminTicketsPlaceholder()
                        AdminScreen.Profile -> AdminProfileScreen(onLogout = onLogout)
                    }
                }
            }
        }
    }
}

@Composable 
fun AdminTicketsPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Próximamente: Gestión de Tickets", style = MaterialTheme.typography.headlineMedium) 
    }
}
