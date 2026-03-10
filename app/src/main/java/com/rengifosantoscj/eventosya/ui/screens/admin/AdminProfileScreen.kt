package com.rengifosantoscj.eventosya.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rengifosantoscj.eventosya.data.preferences.ThemePreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    viewModel: AdminProfileViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { ThemePreferences(context) }
    
    val dynamicColorEnabled by preferences.dynamicColorEnabled.collectAsState(initial = true)
    val highContrastEnabled by preferences.highContrastEnabled.collectAsState(initial = false)
    
    val state = viewModel.uiState
    var isEditingName by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mi Perfil") }) }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (state.user?.name ?: "U").take(1).uppercase(),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nombre y Edición
                if (isEditingName) {
                    OutlinedTextField(
                        value = state.user?.name ?: "",
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("Nombre Completo") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { 
                                viewModel.saveProfile()
                                isEditingName = false 
                            }) {
                                if (state.isSaving) {
                                    CircularProgressIndicator(size = 24.dp)
                                } else {
                                    Icon(Icons.Default.Save, contentDescription = "Guardar")
                                }
                            }
                        }
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.user?.name ?: "Usuario",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { isEditingName = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar nombre", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Text(
                    text = state.user?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                AssistChip(
                    onClick = { },
                    label = { Text(state.user?.role?.uppercase() ?: "USER") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (state.user?.role == "admin") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Configuración
                Text(
                    text = "Configuración de apariencia",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Color dinámico") },
                            supportingContent = { Text("Usar colores del sistema (Android 12+)") },
                            leadingContent = { Icon(Icons.Default.Palette, null) },
                            trailingContent = {
                                Switch(
                                    checked = dynamicColorEnabled,
                                    onCheckedChange = { enabled ->
                                        scope.launch { preferences.setDynamicColorEnabled(enabled) }
                                    }
                                )
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        ListItem(
                            headlineContent = { Text("Alto contraste") },
                            supportingContent = { Text("Mejorar visibilidad de elementos") },
                            leadingContent = { Icon(Icons.Default.Contrast, null) },
                            trailingContent = {
                                Switch(
                                    checked = highContrastEnabled,
                                    onCheckedChange = { enabled ->
                                        scope.launch { preferences.setHighContrastEnabled(enabled) }
                                    }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar Sesión")
                }

                // Snackbars para feedback
                if (state.errorMessage != null) {
                    Text(state.errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
                }
                if (state.successMessage != null) {
                    Text(state.successMessage, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
                    LaunchedEffect(state.successMessage) {
                        kotlinx.coroutines.delay(3000)
                        viewModel.clearMessages()
                    }
                }
            }
        }
    }
}

@Composable
private fun CircularProgressIndicator(size: androidx.compose.ui.unit.Dp) {
    androidx.compose.material3.CircularProgressIndicator(
        modifier = Modifier.size(size),
        strokeWidth = 2.dp
    )
}
