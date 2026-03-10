package com.rengifosantoscj.eventosya.ui.screens.admin

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.rengifosantoscj.eventosya.data.model.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String,
    viewModel: EventDetailsViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToTicket: (String) -> Unit
) {
    val state = viewModel.uiState

    LaunchedEffect(eventId) {
        viewModel.loadEventDetails(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Evento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is EventDetailsUiState.Loading -> CircularProgressIndicator()
                is EventDetailsUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is EventDetailsUiState.Success -> {
                    EventContent(
                        event = state.event,
                        isAdmin = state.isAdmin,
                        isRegistered = state.isRegistered,
                        ticketId = state.ticketId,
                        onToggleRegistration = { viewModel.toggleRegistration(eventId) },
                        onViewTicket = { state.ticketId?.let { onNavigateToTicket(it) } }
                    )
                }
            }
        }
    }
}

@Composable
fun EventContent(
    event: Event,
    isAdmin: Boolean,
    isRegistered: Boolean,
    ticketId: String?,
    onToggleRegistration: () -> Unit,
    onViewTicket: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = event.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        SuggestionChip(onClick = {}, label = { Text(event.category) })

        Spacer(modifier = Modifier.height(16.dp))

        // Mapa (Ubicación)
        if (event.latitude != null && event.longitude != null) {
            val location = LatLng(event.latitude, event.longitude)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(location, 15f)
            }
            
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Map, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ubicación en el mapa", style = MaterialTheme.typography.titleSmall)
                    }
                    TextButton(onClick = {
                        val gmmIntentUri = Uri.parse("google.navigation:q=${event.latitude},${event.longitude}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    }) {
                        Icon(Icons.Default.Navigation, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cómo llegar")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = MarkerState(position = location),
                        title = event.location
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        InfoRow(Icons.Default.CalendarMonth, "Fecha", event.date)
        InfoRow(Icons.Default.LocationOn, "Lugar", event.location)
        InfoRow(Icons.Default.Payments, "Precio", if (event.price == 0.0) "Gratis" else "$${event.price}")
        
        Spacer(modifier = Modifier.height(24.dp))

        Text("Sobre el evento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = event.description, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(32.dp))

        if (isAdmin) {
            AdminActions(event)
        } else {
            if (isRegistered && ticketId != null) {
                OutlinedButton(
                    onClick = onViewTicket,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.ConfirmationNumber, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver mi Ticket QR")
                }
            }
            UserActions(isRegistered, onToggleRegistration)
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AdminActions(event: Event) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Panel de Administrador", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Inscritos: ${event.registeredUsers.size} / ${event.capacity}")
            LinearProgressIndicator(
                progress = { (event.registeredUsers.size.toFloat() / event.capacity.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun UserActions(isRegistered: Boolean, onToggleRegistration: () -> Unit) {
    Button(
        onClick = onToggleRegistration,
        modifier = Modifier.fillMaxWidth(),
        colors = if (isRegistered) {
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
        } else {
            ButtonDefaults.buttonColors()
        }
    ) {
        Text(if (isRegistered) "Cancelar Inscripción" else "Inscribirme al Evento")
    }
}
