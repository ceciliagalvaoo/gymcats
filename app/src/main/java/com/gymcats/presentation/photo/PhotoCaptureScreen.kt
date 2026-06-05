package com.gymcats.presentation.photo

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCaptureScreen(workoutId: Long, onDone: () -> Unit) {
    val viewModel: PhotoViewModel = hiltViewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var permissionDenied by remember { mutableStateOf(false) }
    var photoCaptured by remember { mutableStateOf(false) }

    fun clearTempPhoto() {
        photoPath?.let { path ->
            runCatching { File(path).takeIf { it.exists() }?.delete() }
        }
    }

    fun prepareImageUri(): Uri {
        clearTempPhoto()
        val file = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg")
        photoPath = file.absolutePath
        photoUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        photoCaptured = false
        return photoUri!!
    }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoCaptured = true
        } else {
            clearTempPhoto()
            photoUri = null
            photoPath = null
            photoCaptured = false
        }
    }

    fun launchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            takePicture.launch(prepareImageUri())
        }
    }

    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            permissionDenied = true
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Foto do treino") }) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (permissionDenied) {
                Text(
                    "Permissao de camera necessaria para adicionar fotos.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
            }

            if (photoCaptured && photoPath != null) {
                Icon(
                    imageVector = Icons.Filled.ThumbUp,
                    contentDescription = "Foto tirada com sucesso",
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Foto tirada com sucesso",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(24.dp))
                OutlinedButton(
                    onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            launchCamera()
                        } else {
                            requestPermission.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tirar outra")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val currentPath = photoPath ?: return@Button
                        scope.launch {
                            viewModel.savePhoto(currentPath, workoutId)
                            Toast.makeText(context, "Treino registrado com sucesso", Toast.LENGTH_SHORT).show()
                            onDone()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continuar")
                }
            } else {
                Text(
                    "Quer registrar uma foto de hoje?",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            launchCamera()
                        } else {
                            requestPermission.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tirar foto")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    Toast.makeText(context, "Treino registrado com sucesso", Toast.LENGTH_SHORT).show()
                    onDone()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Pular")
                }
            }
        }
    }
}
