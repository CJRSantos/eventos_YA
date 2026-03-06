package com.rengifosantoscj.eventosya.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rengifosantoscj.eventosya.data.preferences.ThemePreferences
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { ThemePreferences(context) }
    
    val dynamicColorEnabled by preferences.dynamicColorEnabled.collectAsState(initial = true)
    val highContrastEnabled by preferences.highContrastEnabled.collectAsState(initial = false)
    
    val user = FirebaseAuth.getInstance().currentUser
    var userRole by remember { mutableStateOf("Cargando...") }

    // Consultar el rol real en Firestore
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            try {
                val doc = FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
                userRole = doc.getString("role")?.uppercase() ?: "USER"
            } catch (e: Exception) {
                userRole = "ERROR"
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mi Perfil") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user?.email?.take(1)?.uppercase() ?: "U",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user?.email ?: "Usuario",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            AssistChip(
                onClick = { },
                label = { Text(userRole) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (userRole == "ADMIN") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (userRole == "ADMIN") MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Configuración de apariencia",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.weight(1f))

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
        }
    }
}
