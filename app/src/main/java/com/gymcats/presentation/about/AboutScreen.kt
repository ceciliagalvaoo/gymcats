package com.gymcats.presentation.about

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gymcats.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sobre o GymCats") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            AppHeader()

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoSection(
                    icon = Icons.Filled.ReportProblem,
                    title = "O Problema",
                    body = "Apps de treino não falam sobre ciclo. Apps de ciclo não falam sobre treino. " +
                            "A usuária acaba dividindo sua rotina entre dois ou mais lugares, sem conseguir " +
                            "enxergar a relação entre o que sente e como performa.\n\n" +
                            "Sintomas como fadiga, alterações de humor e cólicas aparecem com alta frequência " +
                            "mesmo em quem treina regularmente — e quanto mais intensos, maior a chance de " +
                            "modificar o treino, reduzir carga ou faltar à sessão."
                )

                StatsRow()

                InfoSection(
                    icon = Icons.Filled.Science,
                    title = "Base Científica",
                    body = "O British Journal of Sports Medicine acompanhou mais de 6.800 mulheres ativas " +
                            "e constatou que sintomas menstruais afetam diretamente a disponibilidade para " +
                            "treinar (BRUINVELS et al., 2021).\n\n" +
                            "O impacto no desempenho varia entre mulheres — dados individuais importam mais " +
                            "do que recomendações genéricas (MCNULTY et al., 2020). Esse desafio se manifesta " +
                            "desde os primeiros anos de vida esportiva, com meninas entre 10 e 16 anos já " +
                            "relatando faltas a treinos e competições (BROWN et al., 2025)."
                )

                InfoSection(
                    icon = Icons.Filled.Lightbulb,
                    title = "A Solução",
                    body = "O GymCats organiza treino ativo, registro de sintomas, progressão de carga e " +
                            "fotos de progresso em um único fluxo. Ao fechar um treino, a usuária registra " +
                            "como se sentiu — energia, disposição, humor, cólica, sono.\n\n" +
                            "O app acompanha a fase atual do ciclo — menstrual, folicular, ovulatória ou " +
                            "lútea — e exibe dicas contextuais adaptadas. Com o tempo, a tela de progresso " +
                            "revela padrões individuais: em quais fases o rendimento cai, quando a energia " +
                            "melhora e como a carga evoluiu por exercício."
                )

                FeaturesSection()

                ReferencesSection()

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AppHeader() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_gymcats),
                contentDescription = "GymCats logo",
                modifier = Modifier.size(96.dp)
            )
            Text(
                text = "GymCats",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Treino e ciclo menstrual em um único lugar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            value = "6.800+",
            label = "atletas acompanhadas no estudo principal",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "3",
            label = "pesquisas científicas de base",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeaturesSection() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(icon = Icons.Filled.Star, title = "Funcionalidades")
            listOf(
                Icons.Filled.FitnessCenter to "Treino ativo com timer e CRUD de exercícios",
                Icons.Filled.Search to "Busca de exercícios via API com instruções traduzidas",
                Icons.Filled.Timeline to "Registro de sintomas ao fechar cada treino",
                Icons.Filled.CameraAlt to "Fotos de progresso vinculadas ao treino",
                Icons.Filled.BarChart to "Analytics históricos por período e fase do ciclo",
                Icons.Filled.Notifications to "Lembretes de treino, ciclo e sintomas",
            ).forEach { (icon, text) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ReferencesSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(icon = Icons.Filled.MenuBook, title = "Referências")
            ReferenceItem(
                authors = "BRUINVELS et al. (2021)",
                title = "Prevalence and frequency of menstrual cycle symptoms are associated with availability to train and compete: a study of 6812 exercising women.",
                journal = "British Journal of Sports Medicine, v. 55, n. 8, p. 438–443."
            )
            ReferenceItem(
                authors = "MCNULTY et al. (2020)",
                title = "The Effects of Menstrual Cycle Phase on Exercise Performance in Eumenorrheic Women: A Systematic Review and Meta-Analysis.",
                journal = "Sports Medicine, v. 50, n. 10, p. 1813–1827."
            )
            ReferenceItem(
                authors = "BROWN et al. (2025)",
                title = "Navigating the impact of early years of menstruation in organised sports, among girls 10–16 years.",
                journal = "Journal of Science and Medicine in Sport."
            )
        }
    }
}

@Composable
private fun InfoSection(icon: ImageVector, title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionHeader(icon = icon, title = title)
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ReferenceItem(authors: String, title: String, journal: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = authors,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = journal,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
