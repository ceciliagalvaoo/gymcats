package com.gymcats.util

object ExerciseTranslator {
    val bodyPart = mapOf(
        "back" to "Costas", "cardio" to "Cardio", "chest" to "Peito",
        "lower arms" to "Antebraço", "lower legs" to "Panturrilha", "neck" to "Pescoço",
        "shoulders" to "Ombros", "upper arms" to "Bíceps e Tríceps",
        "upper legs" to "Quadríceps e Glúteos", "waist" to "Abdômen"
    )

    val target = mapOf(
        "abductors" to "Abdutores", "abs" to "Abdômen", "adductors" to "Adutores",
        "biceps" to "Bíceps", "calves" to "Panturrilha", "cardiovascular system" to "Cardio",
        "delts" to "Deltoides", "forearms" to "Antebraço", "glutes" to "Glúteos",
        "hamstrings" to "Isquiotibiais", "lats" to "Latíssimo",
        "levator scapulae" to "Trapézio", "pectorals" to "Peitoral", "quads" to "Quadríceps",
        "serratus anterior" to "Serrátil", "spine" to "Coluna", "traps" to "Trapézio",
        "triceps" to "Tríceps", "upper back" to "Parte sup. das costas"
    )

    val equipment = mapOf(
        "assisted" to "Assistido", "band" to "Elástico", "barbell" to "Barra",
        "body weight" to "Peso corporal", "bosu ball" to "Bosu", "cable" to "Cabo / Polia",
        "dumbbell" to "Halter", "elliptical machine" to "Elíptico", "ez barbell" to "Barra W",
        "kettlebell" to "Kettlebell", "leverage machine" to "Máquina",
        "medicine ball" to "Bola de peso", "olympic barbell" to "Barra olímpica",
        "resistance band" to "Faixa elástica", "roller" to "Rolo de espuma", "rope" to "Corda",
        "smith machine" to "Smith", "stability ball" to "Bola suíça",
        "stationary bike" to "Bike estacionária", "trap bar" to "Barra hexagonal",
        "weighted" to "Com peso", "wheel roller" to "Roda abdominal"
    )

    fun translateBodyPart(value: String) = bodyPart[value] ?: value
    fun translateTarget(value: String) = target[value] ?: value
    fun translateEquipment(value: String) = equipment[value] ?: value
}
