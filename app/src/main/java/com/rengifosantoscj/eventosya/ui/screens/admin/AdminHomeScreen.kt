package com.rengifosantoscj.eventosya.ui.screens.admin

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rengifosantoscj.eventosya.data.model.Event
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    viewModel: AdminHomeViewModel = viewModel(),
    onNavigateToCreate: () -> Unit
) {
    val state = viewModel.uiState
    val user = FirebaseAuth.getInstance().currentUser
    var isAdmin by remember { mutableStateOf(false) }

    // Verificar si el usuario es Admin realmente
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            try {
                val doc = FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
                val role = doc.getString("role")
                isAdmin = role == "admin"
            } catch (e: Exception) {
                isAdmin = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eventos Disponibles") },
                actions = {
                    IconButton(onClick = viewModel::fetchEvents) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                }
            )
        },
        // EL BOTÓN SOLO SE MUESTRA SI ES ADMIN
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = onNavigateToCreate) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo Evento")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = state,
                label = "AdminHomeTransition"
            ) { targetState ->
                when (targetState) {
                    is AdminHomeUiState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is AdminHomeUiState.Success -> {
                        EventList(
                            events = targetState.events,
                            onDelete = viewModel::deleteEvent,
                            showDeleteIcon = isAdmin // Solo admin ve el tachito
                        )
                    }
                    is AdminHomeUiState.Empty -> {
                        Text("No hay eventos creados todavía")
                    }
                    is AdminHomeUiState.Error -> {
                        Text("Error: ${targetState.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun EventList(
    events: List<Event>,
    onDelete: (String) -> Unit,
    showDeleteIcon: Boolean // Nuevo parámetro
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(events) { event ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(event.date, style = MaterialTheme.typography.bodySmall)
                        Text(event.location, style = MaterialTheme.typography.bodySmall)
                    }
                    if (showDeleteIcon) { // Solo se muestra si es Admin
                        IconButton(onClick = { onDelete(event.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
