package com.gymcats.util

import android.content.Context
import android.content.Intent

object ShareHelper {
    fun shareProgress(
        context: Context,
        periodLabel: String,
        workoutsCount: Int,
        mostFrequent: String,
        avgEnergy: Float,
        topGain: String
    ) {
        val text = """
            Meu progresso no GymCats 🐱💪

            Período: $periodLabel
            ✅ Treinos realizados: $workoutsCount
            💪 Exercício mais frequente: $mostFrequent
            ⚡ Energia média: ${"%.1f".format(avgEnergy)}/5
            🐱 Exercício que mais evoluiu carga: $topGain
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Compartilhar via"))
    }
}
