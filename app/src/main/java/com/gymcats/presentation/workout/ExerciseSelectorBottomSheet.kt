package com.gymcats.presentation.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gymcats.data.remote.models.ExerciseResponse
import com.gymcats.util.ExerciseTranslator

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseSelectorBottomSheet(
    uiState: WorkoutUiState,
    viewModel: WorkoutViewModel,
    onExerciseSelected: (ExerciseResponse) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var filterTab by remember { mutableStateOf(0) }

    if (uiState.selectedExercise != null) {
        ExerciseDetailSheet(
            exercise = uiState.selectedExercise,
            onConfirm = {
                onExerciseSelected(uiState.selectedExercise)
            },
            onBack = { viewModel.clearSelectedExercise() }
        )
        return
    }

    if (uiState.isLoadingExerciseDetail) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelectedExercise() },
            sheetState = sheetState
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Carregando detalhes do exercício...")
                }
            }
        }
        return
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.hideExerciseSelector() },
        sheetState = sheetState
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Buscar exercício", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { viewModel.hideExerciseSelector() }) {
                    Icon(Icons.Filled.Close, "Fechar")
                }
            }
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.searchExercises(it) },
                label = { Text("Buscar por nome") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = filterTab == 1,
                        onClick = { filterTab = 1 },
                        label = { Text("Grupo muscular") }
                    )
                }
                item {
                    FilterChip(
                        selected = filterTab == 2,
                        onClick = { filterTab = 2 },
                        label = { Text("Músculo alvo") }
                    )
                }
                item {
                    FilterChip(
                        selected = filterTab == 3,
                        onClick = { filterTab = 3 },
                        label = { Text("Equipamento") }
                    )
                }
            }

            if (filterTab > 0) {
                Spacer(Modifier.height(8.dp))
                val filterItems = when (filterTab) {
                    1 -> uiState.bodyParts
                    2 -> uiState.targets
                    else -> uiState.equipments
                }
                val filterType = when (filterTab) {
                    1 -> "bodypart"
                    2 -> "target"
                    else -> "equipment"
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterItems) { item ->
                        FilterChip(
                            selected = uiState.selectedFilter == item,
                            onClick = { viewModel.applyFilter(item, filterType) },
                            label = {
                                Text(
                                    when (filterTab) {
                                        1 -> ExerciseTranslator.translateBodyPart(item)
                                        2 -> ExerciseTranslator.translateTarget(item)
                                        else -> ExerciseTranslator.translateEquipment(item)
                                    }
                                )
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            when {
                uiState.isSearching -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                uiState.searchError != null -> Text(
                    uiState.searchError,
                    color = MaterialTheme.colorScheme.error
                )
                uiState.searchResults.isEmpty() && uiState.searchQuery.length >= 2 ->
                    Text("Nenhum resultado encontrado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> LazyColumn(Modifier.height(300.dp)) {
                    items(uiState.searchResults) { exercise ->
                        ListItem(
                            headlineContent = { Text(exercise.name) },
                            supportingContent = {
                                Text(
                                    "${ExerciseTranslator.translateBodyPart(exercise.bodyPart)} · ${ExerciseTranslator.translateEquipment(exercise.equipment)}"
                                )
                            },
                            modifier = Modifier.clickable { viewModel.selectExercise(exercise) }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseDetailSheet(
    exercise: ExerciseResponse,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onBack) {
        LazyColumn(Modifier.padding(16.dp)) {
            item {
                TextButton(onClick = onBack) { Text("Voltar") }
                Text(exercise.name, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${ExerciseTranslator.translateBodyPart(exercise.bodyPart)} · ${ExerciseTranslator.translateTarget(exercise.target)} · ${ExerciseTranslator.translateEquipment(exercise.equipment)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
            }
            if (exercise.gifUrl != null) {
                item {
                    AsyncImage(
                        model = exercise.gifUrl,
                        contentDescription = exercise.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
            if (exercise.instructions.isNotEmpty()) {
                item { Text("Instruções:", style = MaterialTheme.typography.titleSmall) }
                items(exercise.instructions.take(5)) { step ->
                    Text(
                        "• $step",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
                    Text("Adicionar ao treino")
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
