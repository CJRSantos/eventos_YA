package com.rengifosantoscj.eventosya.ui.screens.admin

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rengifosantoscj.eventosya.data.model.Ticket
import kotlinx.coroutines.tasks.await

sealed class AdminScreen(val route: String, val label: String, val icon: ImageVector) {
    object Events : AdminScreen("admin_events", "Eventos", Icons.Default.Event)
    object CreateEvent : AdminScreen("create_event", "Nuevo Evento", Icons.Default.Event)
    data class EventDetails(val eventId: String) : AdminScreen("event_details", "Detalles", Icons.Default.Info)
    data class TicketDetail(val ticketId: String) : AdminScreen("ticket", "Ticket", Icons.Default.QrCode)
    object Scanner : AdminScreen("scanner", "Escanear", Icons.Default.QrCodeScanner)
    object Tickets : AdminScreen("admin_tickets", "Tickets", Icons.Default.ConfirmationNumber)
    object Profile : AdminScreen("admin_profile", "Perfil", Icons.Default.Person)
}

@Composable
fun AdminNavigationContainer(
    onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf<AdminScreen>(AdminScreen.Events) }
    val screens = listOf(AdminScreen.Events, AdminScreen.Tickets, AdminScreen.Profile)
    
    val user = FirebaseAuth.getInstance().currentUser
    var userRole by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            val doc = FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
            userRole = doc.getString("role")
        }
    }

    BoxWithConstraints {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            bottomBar = {
                val showBottomBar = !useRail && currentScreen !is AdminScreen.CreateEvent && 
                                   currentScreen !is AdminScreen.EventDetails && 
                                   currentScreen !is AdminScreen.TicketDetail &&
                                   currentScreen !is AdminScreen.Scanner
                if (showBottomBar) {
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
                val showRail = useRail && currentScreen !is AdminScreen.CreateEvent && 
                               currentScreen !is AdminScreen.EventDetails && 
                               currentScreen !is AdminScreen.TicketDetail &&
                               currentScreen !is AdminScreen.Scanner
                if (showRail) {
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
                        is AdminScreen.Events -> {
                            AdminHomeScreen(
                                onNavigateToCreate = { currentScreen = AdminScreen.CreateEvent },
                                onNavigateToDetails = { id: String -> currentScreen = AdminScreen.EventDetails(id) }
                            )
                        }
                        is AdminScreen.CreateEvent -> {
                            CreateEventScreen(
                                onNavigateBack = { currentScreen = AdminScreen.Events }
                            )
                        }
                        is AdminScreen.EventDetails -> {
                            EventDetailsScreen(
                                eventId = screen.eventId,
                                onNavigateBack = { currentScreen = AdminScreen.Events },
                                onNavigateToTicket = { id: String -> currentScreen = AdminScreen.TicketDetail(id) }
                            )
                        }
                        is AdminScreen.TicketDetail -> {
                            TicketScreen(
                                ticketId = screen.ticketId,
                                onNavigateBack = { currentScreen = AdminScreen.Events }
                            )
                        }
                        is AdminScreen.Scanner -> {
                            AdminScannerScreen(
                                onNavigateBack = { currentScreen = AdminScreen.Events }
                            )
                        }
                        is AdminScreen.Tickets -> {
                            if (userRole == "admin") {
                                AdminTicketsPanel(
                                    onNavigateToScanner = { currentScreen = AdminScreen.Scanner }
                                )
                            } else {
                                UserTicketsList(
                                    onNavigateToTicket = { id -> currentScreen = AdminScreen.TicketDetail(id) }
                                )
                            }
                        }
                        is AdminScreen.Profile -> AdminProfileScreen(onLogout = onLogout)
                    }
                }
            }
        }
    }
}

@Composable 
fun AdminTicketsPanel(onNavigateToScanner: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Panel de Control de Tickets", style = MaterialTheme.typography.headlineMedium)
            Text("Modo Administrador", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNavigateToScanner,
                modifier = Modifier.height(56.dp).padding(horizontal = 32.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ABRIR ESCÁNER QR")
            }
        }
    }
}

@Composable
fun UserTicketsList(onNavigateToTicket: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    var tickets by remember { mutableStateOf<List<Ticket>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            val snapshot = db.collection("tickets").whereEqualTo("userId", uid).get().await()
            tickets = snapshot.toObjects(Ticket::class.java)
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mis Tickets", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Presenta estos QRs en la entrada", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (tickets.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Aún no tienes tickets comprados") }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(tickets) { ticket ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigateToTicket(ticket.id) }
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(ticket.eventTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(ticket.date, style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Default.QrCode, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
