package com.gymcats.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymcats.R
import com.gymcats.util.BiometricHelper

@Composable
fun LoginScreen(onAuthenticated: (String) -> Unit) {
    val viewModel: LoginViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.nextRoute) {
        uiState.nextRoute?.let(onAuthenticated)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.biometricLabel) {
        if (uiState.biometricLabel != null && BiometricHelper.canAuthenticate(context)) {
            BiometricHelper.authenticate(
                activity = context as FragmentActivity,
                onSuccess = { viewModel.onBiometricSuccess() },
                onError = { viewModel.onBiometricError(it) }
            )
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_gymcats),
                contentDescription = "Logo GymCats",
                modifier = Modifier.size(112.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "GymCats",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Entre na sua conta ou crie uma nova antes do onboarding.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !uiState.isRegisterMode,
                    onClick = { viewModel.setRegisterMode(false) },
                    label = { Text("Entrar") }
                )
                FilterChip(
                    selected = uiState.isRegisterMode,
                    onClick = { viewModel.setRegisterMode(true) },
                    label = { Text("Criar conta") }
                )
            }
            Spacer(Modifier.height(24.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                if (uiState.biometricLabel != null && BiometricHelper.canAuthenticate(context)) {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = "Biometria",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Usar biometria para ${uiState.biometricLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            BiometricHelper.authenticate(
                                activity = context as FragmentActivity,
                                onSuccess = { viewModel.onBiometricSuccess() },
                                onError = { viewModel.onBiometricError(it) }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Usar biometria")
                    }
                    Spacer(Modifier.height(16.dp))
                }

                if (uiState.isRegisterMode) {
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = viewModel::setEmail,
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.phone,
                        onValueChange = viewModel::setPhone,
                        label = { Text("Numero") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                } else {
                    OutlinedTextField(
                        value = uiState.identifier,
                        onValueChange = viewModel::setIdentifier,
                        label = { Text("Email ou numero") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::setPassword,
                    label = { Text("Senha") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (uiState.isRegisterMode) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "A senha deve ter 8+ caracteres, com maiúscula, minúscula e número.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = viewModel::setConfirmPassword,
                        label = { Text("Confirmar senha") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.submit() }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (uiState.isRegisterMode) "Criar conta" else "Entrar")
                }
                if (!uiState.isRegisterMode) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { viewModel.forgotPassword() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Esqueci minha senha")
                    }
                }
            }
        }
    }
}
