package com.rengifosantoscj.eventosya.ui.screens.admin

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScannerScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCamPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCamPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    var scanResult by remember { mutableStateOf<String?>(null) }
    var isVerifying by remember { mutableStateOf(false) }
    var verificationMessage by remember { mutableStateOf<String?>(null) }

    val db = FirebaseFirestore.getInstance()

    suspend fun verifyTicket(ticketId: String) {
        isVerifying = true
        try {
            val doc = db.collection("tickets").document(ticketId).get().await()
            if (doc.exists()) {
                val status = doc.getString("status")
                if (status == "active") {
                    db.collection("tickets").document(ticketId).update("status", "used").await()
                    verificationMessage = "¡Ticket Válido! Acceso concedido."
                } else {
                    verificationMessage = "Ticket ya utilizado o inválido."
                }
            } else {
                verificationMessage = "Ticket no encontrado."
            }
        } catch (e: Exception) {
            verificationMessage = "Error al verificar: ${e.localizedMessage}"
        } finally {
            isVerifying = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear Ticket") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (hasCamPermission) {
                if (scanResult == null) {
                    Box(modifier = Modifier.weight(1f)) {
                        AndroidView(
                            factory = { context ->
                                val previewView = PreviewView(context)
                                val preview = Preview.Builder().build()
                                val selector = CameraSelector.Builder()
                                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                    .build()
                                preview.setSurfaceProvider(previewView.surfaceProvider)
                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                imageAnalysis.setAnalyzer(
                                    Executors.newSingleThreadExecutor(),
                                    { imageProxy ->
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null) {
                                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                            val scanner = BarcodeScanning.getClient()
                                            scanner.process(image)
                                                .addOnSuccessListener { barcodes ->
                                                    for (barcode in barcodes) {
                                                        barcode.rawValue?.let { 
                                                            scanResult = it
                                                        }
                                                    }
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close()
                                                }
                                        }
                                    }
                                )
                                try {
                                    ProcessCameraProvider.getInstance(context).get().bindToLifecycle(
                                        lifecycleOwner,
                                        selector,
                                        preview,
                                        imageAnalysis
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // Resultado del escaneo
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Ticket Detectado", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (isVerifying) {
                            CircularProgressIndicator()
                        } else if (verificationMessage != null) {
                            Text(verificationMessage!!, color = if (verificationMessage!!.contains("Válido")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { 
                                scanResult = null
                                verificationMessage = null
                            }) {
                                Text("Escanear otro")
                            }
                        } else {
                            LaunchedEffect(scanResult) {
                                verifyTicket(scanResult!!)
                            }
                        }
                    }
                }
            } else {
                Text("Se requiere permiso de cámara para escanear.")
            }
        }
    }
}
