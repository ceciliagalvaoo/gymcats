package com.gymcats.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.gymcats.BuildConfig
import com.gymcats.presentation.navigation.Screen
import com.gymcats.util.NotificationHelper
import com.gymcats.util.ProfileOptions
import java.time.DayOfWeek

private val DAYS_PT = mapOf(
    DayOfWeek.MONDAY to "Seg", DayOfWeek.TUESDAY to "Ter", DayOfWeek.WEDNESDAY to "Qua",
    DayOfWeek.THURSDAY to "Qui", DayOfWeek.FRIDAY to "Sex", DayOfWeek.SATURDAY to "Sáb",
    DayOfWeek.SUNDAY to "Dom"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoggedOut by viewModel.isLoggedOut.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("Perfil salvo!")
            viewModel.clearSaved()
        }
    }
    LaunchedEffect(uiState.debugSeedMessage) {
        uiState.debugSeedMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearDebugSeedMessage()
        }
    }
    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) {
            viewModel.clearLoggedOut()
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Dados pessoais", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.setName(it) },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            GoalDropdown(selected = uiState.goal, onSelect = viewModel::setGoal)

            Text("Ciclo menstrual", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = uiState.lastPeriodDate,
                onValueChange = { viewModel.setLastPeriodDate(it) },
                label = { Text("Início da última menstruação (aaaa-MM-dd)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.cycleLength.toString(),
                onValueChange = { it.toIntOrNull()?.let { v -> viewModel.setCycleLength(v) } },
                label = { Text("Duração do ciclo (dias)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.periodLength.toString(),
                onValueChange = { it.toIntOrNull()?.let { v -> viewModel.setPeriodLength(v) } },
                label = { Text("Duração da menstruação (dias)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Text("Preferências de treino", style = MaterialTheme.typography.titleMedium)
            Text("Dias de treino:", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DayOfWeek.values().forEach { day ->
                    FilterChip(
                        selected = uiState.selectedDays.contains(day),
                        onClick = { viewModel.toggleDay(day) },
                        label = { Text(DAYS_PT[day] ?: day.name) }
                    )
                }
            }
            OutlinedTextField(
                value = uiState.trainingHour.toString(),
                onValueChange = { it.toIntOrNull()?.coerceIn(0, 23)?.let { v -> viewModel.setTrainingHour(v) } },
                label = { Text("Horário habitual de treino (0-23h)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notificações ativas")
                Switch(
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { viewModel.setNotifications(it) }
                )
            }

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) CircularProgressIndicator()
                else Text("Salvar alterações")
            }

            OutlinedButton(
                onClick = {
                    NotificationHelper.send(context, "GymCats", "Isso é uma notificação de teste!", id = 99)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simular notificação")
            }

            if (BuildConfig.DEBUG) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { viewModel.seedDebugData() },
                        enabled = !uiState.isSeedingDebugData
                    ) {
                        if (uiState.isSeedingDebugData) {
                            CircularProgressIndicator(modifier = Modifier.height(18.dp))
                        } else {
                            Text("Popular dados de teste")
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sair da conta")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalDropdown(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Objetivo") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ProfileOptions.goals.forEach { goal ->
                DropdownMenuItem(
                    text = { Text(goal) },
                    onClick = {
                        onSelect(goal)
                        expanded = false
                    }
                )
            }
        }
    }
}

