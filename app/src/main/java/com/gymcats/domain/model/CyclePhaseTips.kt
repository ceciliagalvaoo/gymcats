package com.gymcats.domain.model

fun cyclePhaseTip(phase: CyclePhase, goal: String): String = when (phase) {
    CyclePhase.MENSTRUAL -> when (goal) {
        "Hipertrofia"   -> "Durante a menstruação, priorize volume baixo e pesos moderados. O corpo responde melhor à recuperação ativa nessa fase."
        "Força"         -> "Reduza a intensidade hoje. Séries menores ajudam a manter o estímulo sem sobrecarregar."
        "Emagrecimento" -> "Prefira atividades de baixo impacto. O corpo ainda gasta energia sem precisar forçar o ritmo."
        else            -> "Movimento suave é bem-vindo — caminhada, yoga e mobilidade são ótimas opções nessa fase."
    }
    CyclePhase.FOLICULAR -> when (goal) {
        "Hipertrofia"   -> "Estrogênio em alta favorece síntese proteica. Ótimo momento para aumentar carga e volume."
        "Força"         -> "Fase ideal para bater recordes. Sua força máxima tende a aumentar nesse período."
        "Emagrecimento" -> "Sensibilidade à insulina melhora nessa fase. Combine treino com alimentação equilibrada para potencializar resultados."
        else            -> "Energia renovada — aproveite para criar consistência e experimentar novas atividades."
    }
    CyclePhase.OVULATORIA -> when (goal) {
        "Hipertrofia"   -> "Pico hormonal: seu corpo está no auge para ganho muscular. Treine pesado."
        "Força"         -> "Melhor janela do ciclo para força máxima. Aproveite para testar seus limites com segurança."
        "Emagrecimento" -> "Metabolismo acelerado e alta energia — treinos de alta intensidade têm ótimo retorno agora."
        else            -> "Disposição no máximo. Use essa energia para treinos que você gosta e sente prazer."
    }
    CyclePhase.LUTEA -> when (goal) {
        "Hipertrofia"   -> "Progesterona alta pode reduzir síntese proteica. Mantenha volume moderado e capriche na recuperação."
        "Força"         -> "Foco em técnica e execução. Evite testar máximos, mas mantenha o estímulo consistente."
        "Emagrecimento" -> "O metabolismo basal sobe nessa fase. Calorias extras são queimadas mesmo em repouso — mantenha o treino moderado."
        else            -> "Escute seu corpo. Treinos de bem-estar, como yoga ou pilates, são excelentes escolhas agora."
    }
}
