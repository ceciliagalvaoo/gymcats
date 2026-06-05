package com.gymcats.domain.model

enum class CyclePhase(val label: String, val tip: String) {
    MENSTRUAL("Menstrual", "Priorize recuperação e treinos leves."),
    FOLICULAR("Folicular", "Boa fase para treinos intensos e novos recordes."),
    OVULATORIA("Ovulatória", "Pico de energia, aproveite para treinos pesados."),
    LUTEA("Lútea", "Foque em técnica, mobilidade e bem-estar.")
}
