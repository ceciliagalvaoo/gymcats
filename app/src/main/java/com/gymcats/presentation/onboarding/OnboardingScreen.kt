package com.gymcats.presentation.onboarding

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymcats.util.ProfileOptions
import java.time.DayOfWeek

private val DAYS_PT = mapOf(
    DayOfWeek.MONDAY to "Seg",
    DayOfWeek.TUESDAY to "Ter",
    DayOfWeek.WEDNESDAY to "Qua",
    DayOfWeek.THURSDAY to "Qui",
    DayOfWeek.FRIDAY to "Sex",
    DayOfWeek.SATURDAY to "Sáb",
    DayOfWeek.SUNDAY to "Dom"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isDone) {
        if (uiState.isDone) onFinish()
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configurar GymCats - Passo ${uiState.step + 1}/3") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (uiState.step) {
                0 -> StepBasicInfo(uiState, viewModel)
                1 -> StepCycleInfo(uiState, viewModel)
                2 -> StepTrainingPrefs(uiState, viewModel)
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.step > 0) {
                    OutlinedButton(
                        onClick = { viewModel.prevStep() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Voltar") }
                }
                Button(
                    onClick = {
                        if (uiState.step < 2) viewModel.nextStep()
                        else viewModel.saveProfile()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) CircularProgressIndicator()
                    else Text(if (uiState.step < 2) "Próximo" else "Concluir")
                }
            }
        }
    }
}

@Composable
private fun StepBasicInfo(uiState: OnboardingUiState, viewModel: OnboardingViewModel) {
    Text("Sobre você", style = MaterialTheme.typography.titleLarge)
    OutlinedTextField(
        value = uiState.name,
        onValueChange = { viewModel.setName(it) },
        label = { Text("Nome") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    GoalDropdown(uiState.goal, viewModel::setGoal)
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
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
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

@Composable
private fun StepCycleInfo(uiState: OnboardingUiState, viewModel: OnboardingViewModel) {
    Text("Seu ciclo menstrual", style = MaterialTheme.typography.titleLarge)
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepTrainingPrefs(uiState: OnboardingUiState, viewModel: OnboardingViewModel) {
    Text("Preferências de treino", style = MaterialTheme.typography.titleLarge)
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
}

