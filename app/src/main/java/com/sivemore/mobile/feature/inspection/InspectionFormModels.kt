package com.sivemore.mobile.feature.inspection

data class InspectionQuestionOption(
    val id: String,
    val label: String,
)

data class InspectionQuestionItem(
    val id: String,
    val title: String,
    val options: List<InspectionQuestionOption>,
    val selectedOptionId: String? = null,
)

data class InspectionSectionUiState(
    val id: String,
    val title: String,
    val description: String,
    val questions: List<InspectionQuestionItem>,
) {
    val isComplete: Boolean
        get() = questions.all { it.selectedOptionId != null }
}

object InspectionSectionCatalog {
    private val generalLightOptions = listOf(
        InspectionQuestionOption(id = "APPROVED", label = "Aprobadas"),
        InspectionQuestionOption(id = "LEFT_BURNT", label = "Izquierda fundida"),
        InspectionQuestionOption(id = "RIGHT_BURNT", label = "Derecha fundida"),
        InspectionQuestionOption(id = "BOTH_BURNT", label = "Ambas fundidas"),
    )

    private val indicatorsOptions = listOf(
        InspectionQuestionOption(id = "APPROVED", label = "Aprobadas"),
        InspectionQuestionOption(id = "ONE_BURNT", label = "1 fundida"),
        InspectionQuestionOption(id = "TWO_BURNT", label = "2 fundidas"),
        InspectionQuestionOption(id = "THREE_BURNT", label = "3 fundidas"),
    )

    private val headlightOptions = listOf(
        InspectionQuestionOption(id = "APPROVED", label = "Aprobado"),
        InspectionQuestionOption(id = "LOOSE", label = "Flojo"),
        InspectionQuestionOption(id = "BROKEN", label = "Roto"),
    )

    fun lucesSection(): InspectionSectionUiState = InspectionSectionUiState(
        id = "luces",
        title = "Luces",
        description = "Completa la inspeccion de luces antes de avanzar a la siguiente seccion.",
        questions = listOf(
            InspectionQuestionItem(id = "luces_galibo", title = "Luces galibo", options = generalLightOptions),
            InspectionQuestionItem(id = "luces_altas", title = "Luces altas", options = generalLightOptions),
            InspectionQuestionItem(id = "luces_bajas", title = "Luces bajas", options = generalLightOptions),
            InspectionQuestionItem(
                id = "luces_demarcadoras_delanteras",
                title = "Luces demarcadoras delanteras",
                options = generalLightOptions,
            ),
            InspectionQuestionItem(
                id = "luces_demarcadoras_traseras",
                title = "Luces demarcadoras traseras",
                options = generalLightOptions,
            ),
            InspectionQuestionItem(id = "luces_indicadoras", title = "Luces indicadoras", options = indicatorsOptions),
            InspectionQuestionItem(id = "faro_izquierdo", title = "Faro izquierdo", options = headlightOptions),
            InspectionQuestionItem(id = "faro_derecho", title = "Faro derecho", options = headlightOptions),
            InspectionQuestionItem(
                id = "luces_direccionales_delanteras",
                title = "Luces direccionales delanteras",
                options = generalLightOptions,
            ),
            InspectionQuestionItem(
                id = "luces_direccionales_traseras",
                title = "Luces direccionales traseras",
                options = generalLightOptions,
            ),
        ),
    )
}
