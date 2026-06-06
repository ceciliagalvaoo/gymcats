package com.gymcats.presentation.progress

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gymcats.presentation.navigation.Screen
import com.gymcats.util.DateUtils
import com.gymcats.util.ShareHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProgressScreen(navController: NavController) {
    val viewModel: ProgressViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progresso") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        ShareHelper.shareProgress(
                            context,
                            "últimos ${uiState.periodMonths} mes(es)",
                            uiState.totalWorkouts,
                            uiState.mostFrequentExercise,
                            uiState.avgEnergy,
                            uiState.topGain
                        )
                    }) {
                        Icon(Icons.Filled.Share, "Compartilhar")
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

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("Período", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1 to "1 mes", 3 to "3 meses", 6 to "6 meses").forEach { (months, label) ->
                        FilterChip(
                            selected = uiState.periodMonths == months,
                            onClick = { viewModel.setPeriod(months) },
                            label = { Text(label) }
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            value = uiState.totalWorkouts.toString(),
                            label = "treinos",
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            value = "%.1f".format(uiState.avgEnergy),
                            label = "energia média",
                            valueColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            value = "%.1f".format(uiState.avgDisposition),
                            label = "disposição média",
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            value = "%.1f".format(uiState.avgSleepQuality),
                            label = "sono médio",
                            valueColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        MetricCard(
                            modifier = Modifier.fillMaxWidth(0.48f),
                            value = "${uiState.avgWorkoutMinutes} min",
                            label = "tempo médio",
                            valueColor = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            if (uiState.avgCrampsByPhase.values.any { it > 0f }) {
                item {
                    Text("Cólica média ao longo do ciclo", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    PhaseBarChart(
                        values = uiState.avgCrampsByPhase,
                        maxValue = 5f,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (uiState.moodDistributionByPhase.values.any { it.isNotEmpty() }) {
                item {
                    Text("Humor por fase do ciclo", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    MoodDistributionChart(uiState.moodDistributionByPhase)
                }
            }

            if (uiState.exerciseNames.isEmpty()) {
                item {
                    Text(
                    "Nenhum treino registrado ainda. Que tal começar hoje?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                item { Text("Progressão por exercício", style = MaterialTheme.typography.titleSmall) }
                items(uiState.exerciseNames) { name ->
                    ListItem(
                        headlineContent = { Text(name) },
                        modifier = Modifier.clickable {
                            navController.navigate(
                                Screen.ExerciseProgression.createRoute(name, uiState.periodMonths)
                            )
                        }
                    )
                }
            }

            if (uiState.photos.isNotEmpty()) {
                item { Text("Fotos do período", style = MaterialTheme.typography.titleSmall) }
                items(uiState.photos) { photo ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = resolvePhotoModel(photo.imagePath, context.filesDir),
                                contentDescription = "Foto de progresso",
                                modifier = Modifier.size(80.dp)
                            )
                            Column {
                                Text(DateUtils.formatDisplay(photo.date), style = MaterialTheme.typography.bodySmall)
                                photo.workoutName?.takeIf { it.isNotBlank() }?.let {
                                    Text(it, style = MaterialTheme.typography.bodyMedium)
                                }
                                photo.cycleNotes?.takeIf { it.isNotBlank() }?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun MoodDistributionChart(distributions: Map<String, Map<String, Int>>) {
    val palette = mapOf(
        "Exausta" to Color(0xFF8E24AA),
        "Cansada" to Color(0xFFE53935),
        "Neutra" to Color(0xFF9E9D24),
        "Animada" to Color(0xFF00897B),
        "Euforica" to Color(0xFF1E88E5)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val legendItems = palette.entries.toList()
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            legendItems.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { (mood, color) ->
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text(mood) },
                            leadingIcon = {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(color)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        distributions.filterValues { it.isNotEmpty() }.forEach { (phase, moods) ->
            val total = moods.values.sum().coerceAtLeast(1)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(phase, style = MaterialTheme.typography.bodyMedium)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .height(16.dp)
                ) {
                    Row(Modifier.fillMaxWidth()) {
                        palette.forEach { (mood, color) ->
                            val count = moods[mood] ?: 0
                            if (count > 0) {
                                Box(
                                    Modifier
                                        .weight(count.toFloat())
                                        .background(color)
                                        .height(16.dp)
                                )
                            }
                        }
                    }
                }
                Text(
                    moods.entries.joinToString("  ") {
                        val percentage = (it.value.toFloat() / total.toFloat()) * 100f
                        "${it.key}: ${percentage.toInt()}%"
                    },
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

private fun resolvePhotoModel(imagePath: String, filesDir: File): Any {
    val directFile = File(imagePath)
    if (directFile.exists()) return directFile

    val fallbackFile = File(filesDir, directFile.name)
    if (fallbackFile.exists()) return fallbackFile

    return Uri.parse(imagePath)
}

@Composable
private fun MetricCard(
    modifier: Modifier,
    value: String,
    label: String,
    valueColor: Color
) {
    Card(modifier) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineMedium,
                color = valueColor,
                textAlign = TextAlign.Center
            )
            Text(
                label,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PhaseBarChart(
    values: Map<String, Float>,
    maxValue: Float,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        values.forEach { (phase, rawValue) ->
            val value = rawValue.coerceIn(0f, maxValue)
            val fraction = (value / maxValue).coerceIn(0f, 1f)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(phase, style = MaterialTheme.typography.bodyMedium)
                    Text("%.1f/5".format(value), style = MaterialTheme.typography.labelMedium)
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .height(12.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(fraction)
                            .clip(RoundedCornerShape(999.dp))
                            .background(color)
                            .height(12.dp)
                    )
                }
            }
        }
    }
}
