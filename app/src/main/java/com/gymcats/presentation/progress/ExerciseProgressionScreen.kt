package com.gymcats.presentation.progress

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.gymcats.data.local.dao.ExerciseProgression
import com.gymcats.domain.usecase.GetExerciseProgressionUseCase
import com.gymcats.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseProgressionUiState(
    val progressions: List<ExerciseProgression> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ExerciseProgressionViewModel @Inject constructor(
    private val getExerciseProgressionUseCase: GetExerciseProgressionUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExerciseProgressionUiState())
    val uiState: StateFlow<ExerciseProgressionUiState> = _uiState.asStateFlow()

    fun load(exerciseName: String, months: Int) {
        viewModelScope.launch {
            _uiState.value = ExerciseProgressionUiState(isLoading = true)
            val data = getExerciseProgressionUseCase(exerciseName, months)
            _uiState.value = ExerciseProgressionUiState(progressions = data, isLoading = false)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseProgressionScreen(
    exerciseName: String,
    months: Int,
    navController: NavController
) {
    val viewModel: ExerciseProgressionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(exerciseName, months) { viewModel.load(exerciseName, months) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$exerciseName - $months mes(es)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.progressions.size <= 1) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Registre esse exercício mais vezes para ver a progressão.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            items(uiState.progressions) { progression ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            DateUtils.formatDisplay(progression.date),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "${progression.weight} kg",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
