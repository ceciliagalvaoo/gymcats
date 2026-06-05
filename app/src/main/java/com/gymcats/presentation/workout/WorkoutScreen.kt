package com.gymcats.presentation.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.gymcats.data.local.entities.ExerciseLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    onWorkoutClosed: (workoutId: Long, startTimeMs: Long) -> Unit,
    onBack: () -> Unit
) {
    val viewModel: WorkoutViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var workoutName by remember { mutableStateOf("Treino") }
    var showStartDialog by remember { mutableStateOf(false) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<ExerciseLog?>(null) }
    var draftExerciseEntry by remember { mutableStateOf(ExerciseEntry(exerciseName = "")) }

    LaunchedEffect(uiState.workout) {
        if (uiState.workout == null && !uiState.workoutCancelled) showStartDialog = true
    }

    LaunchedEffect(uiState.closedWorkoutId) {
        uiState.closedWorkoutId?.let {
            viewModel.clearClosedWorkoutId()
            onWorkoutClosed(it, uiState.closedStartTimeMs)
        }
    }

    LaunchedEffect(uiState.workoutCancelled) {
        if (uiState.workoutCancelled) {
            viewModel.clearWorkoutCancelled()
            onBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showStartDialog && uiState.workout == null) {
        AlertDialog(
            onDismissRequest = { onBack() },
            title = { Text("Novo treino") },
            text = {
                OutlinedTextField(
                    value = workoutName,
                    onValueChange = { workoutName = it },
                    label = { Text("Nome do treino") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.startNewWorkout(workoutName)
                    showStartDialog = false
                }) {
                    Text("Iniciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { onBack() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    LaunchedEffect(editingExercise) {
        editingExercise?.let {
            draftExerciseEntry = ExerciseEntry(
                exerciseName = it.exerciseName,
                exerciseApiId = it.exerciseApiId,
                muscleGroup = it.muscleGroup,
                sets = it.sets.toString(),
                reps = it.reps.toString(),
                weight = it.weight.toString()
            )
        }
    }

    if (showAddExerciseDialog || editingExercise != null) {
        val exerciseToEdit = editingExercise
        AddExerciseDialog(
            initialEntry = draftExerciseEntry,
            isEditing = exerciseToEdit != null,
            submitLabel = if (exerciseToEdit == null) "Adicionar" else "Salvar",
            onEntryChange = { draftExerciseEntry = it },
            onConfirm = { entry ->
                if (exerciseToEdit == null) {
                    viewModel.addExercise(entry)
                    showAddExerciseDialog = false
                    draftExerciseEntry = ExerciseEntry(exerciseName = "")
                } else {
                    viewModel.updateExercise(exerciseToEdit, entry)
                    editingExercise = null
                    draftExerciseEntry = ExerciseEntry(exerciseName = "")
                }
            },
            onDismiss = {
                showAddExerciseDialog = false
                editingExercise = null
                draftExerciseEntry = ExerciseEntry(exerciseName = "")
            },
            onOpenSelector = {
                showAddExerciseDialog = false
                viewModel.showExerciseSelector()
            }
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar treino") },
            text = { Text("Esse treino aberto será descartado. Deseja continuar?") },
            confirmButton = {
                Button(onClick = {
                    showCancelDialog = false
                    viewModel.cancelWorkout()
                }) {
                    Text("Cancelar treino")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Voltar")
                }
            }
        )
    }

    if (uiState.showExerciseSelector) {
        ExerciseSelectorBottomSheet(
            uiState = uiState,
            viewModel = viewModel,
            onExerciseSelected = {
                draftExerciseEntry = draftExerciseEntry.copy(
                    exerciseName = it.name,
                    exerciseApiId = it.id,
                    muscleGroup = it.bodyPart
                )
                viewModel.hideExerciseSelector()
                showAddExerciseDialog = true
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.workout?.name ?: "Treino") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            val elapsed = uiState.elapsedSeconds
            val mins = elapsed / 60
            val secs = elapsed % 60
            Text(
                text = "Tempo: %02d:%02d".format(mins, secs),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Fase: ${uiState.currentPhase.label}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(16.dp))

            if (uiState.exercises.isEmpty()) {
                Text(
                    "Nenhum exercício adicionado ainda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.exercises) { log ->
                        ExerciseLogCard(
                            log = log,
                            onEdit = { editingExercise = log },
                            onDelete = { viewModel.removeExercise(log) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    draftExerciseEntry = ExerciseEntry(exerciseName = "")
                    showAddExerciseDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, null)
                Spacer(Modifier.padding(4.dp))
                Text("Adicionar exercício")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showCancelDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar treino")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.closeWorkout() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Encerrar treino")
            }
        }
    }
}

@Composable
private fun ExerciseLogCard(
    log: ExerciseLog,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(log.exerciseName, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${log.sets}x${log.reps} - ${log.weight}kg",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, "Remover", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun AddExerciseDialog(
    initialEntry: ExerciseEntry,
    isEditing: Boolean,
    submitLabel: String,
    onEntryChange: (ExerciseEntry) -> Unit,
    onConfirm: (ExerciseEntry) -> Unit,
    onDismiss: () -> Unit,
    onOpenSelector: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Editar exercício" else "Adicionar exercício") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = initialEntry.exerciseName,
                    onValueChange = { onEntryChange(initialEntry.copy(exerciseName = it)) },
                    label = { Text("Nome do exercício") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!isEditing) {
                    TextButton(onClick = onOpenSelector) {
                        Text("Buscar na API ->")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = initialEntry.sets,
                        onValueChange = { onEntryChange(initialEntry.copy(sets = it)) },
                        label = { Text("Séries") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = initialEntry.reps,
                        onValueChange = { onEntryChange(initialEntry.copy(reps = it)) },
                        label = { Text("Reps") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = initialEntry.weight,
                    onValueChange = { onEntryChange(initialEntry.copy(weight = it)) },
                    label = { Text("Carga (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (initialEntry.exerciseName.isNotBlank()) {
                    onConfirm(initialEntry)
                }
            }) {
                Text(submitLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

