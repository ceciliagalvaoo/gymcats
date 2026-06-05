package com.gymcats.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymcats.R
import androidx.navigation.NavController
import com.gymcats.presentation.navigation.Screen
import com.gymcats.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GymCats") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Filled.Person, contentDescription = "Perfil")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_gymcats),
                    contentDescription = "Logo GymCats",
                    modifier = Modifier.size(88.dp)
                )
            }

            uiState.profile?.let { profile ->
                Text(
                    "Olá, ${profile.name}!",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }

            uiState.currentPhase?.let { phase ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Fase atual: ${phase.label}", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(phase.tip, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (uiState.showCycleDateWarning) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Atualize a data do seu ciclo no perfil para manter os cálculos precisos.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Treinos este mês",
                        value = uiState.workoutsThisMonth.toString(),
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Check-in de hoje",
                        value = if (uiState.isTodayCheckInDone) "Feito" else "Pendente",
                        valueColor = if (uiState.isTodayCheckInDone) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        }
                    )
                }

                uiState.lastWorkout?.let { workout ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        SummaryCard(
                            modifier = Modifier.fillMaxWidth(0.72f),
                            title = "Último treino",
                            value = workout.name,
                            subtitle = DateUtils.formatDisplay(workout.date)
                        )
                    }
                }
            }

            if (uiState.lastWorkout == null && !uiState.isLoading) {
                Text(
                    "Nenhum treino registrado ainda. Que tal começar hoje?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { navController.navigate(Screen.Workout.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.FitnessCenter, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("Iniciar treino")
            }

            FilledTonalButton(
                onClick = { navController.navigate(Screen.Progress.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Timeline, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("Ver progresso")
            }

            FilledTonalButton(
                onClick = { navController.navigate(Screen.About.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Conhe\u00E7a mais sobre o GymCats")
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String? = null,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineSmall,
                color = valueColor,
                textAlign = TextAlign.Center
            )
            subtitle?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    it,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

