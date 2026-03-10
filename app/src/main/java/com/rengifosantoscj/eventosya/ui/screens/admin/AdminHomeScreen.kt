package com.rengifosantoscj.eventosya.ui.screens.admin

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
    onNavigateToCreate: () -> Unit,
    onNavigateToDetails: (String) -> Unit
) {
    val state = viewModel.uiState
    val user = FirebaseAuth.getInstance().currentUser
    var isAdmin by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

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
                title = {
                    if (isSearchExpanded) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar eventos...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                    } else {
                        Text("Eventos Disponibles")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        isSearchExpanded = !isSearchExpanded 
                        if (!isSearchExpanded) searchQuery = ""
                    }) {
                        Icon(
                            if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search, 
                            contentDescription = "Buscar"
                        )
                    }
                    IconButton(onClick = viewModel::fetchEvents) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                }
            )
        },
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
                        val filteredEvents = if (searchQuery.isEmpty()) {
                            targetState.events
                        } else {
                            targetState.events.filter { 
                                it.title.contains(searchQuery, ignoreCase = true) || 
                                it.description.contains(searchQuery, ignoreCase = true) 
                            }
                        }

                        if (filteredEvents.isEmpty()) {
                            Text("No se encontraron eventos")
                        } else {
                            EventList(
                                events = filteredEvents,
                                onDelete = viewModel::deleteEvent,
                                onEventClick = onNavigateToDetails,
                                showDeleteIcon = isAdmin
                            )
                        }
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
    onEventClick: (String) -> Unit,
    showDeleteIcon: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(events) { event ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEventClick(event.id) },
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
                        Spacer(modifier = Modifier.height(4.dp))
                        SuggestionChip(
                            onClick = {},
                            label = { Text(event.category, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                    if (showDeleteIcon) {
                        IconButton(onClick = { onDelete(event.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
