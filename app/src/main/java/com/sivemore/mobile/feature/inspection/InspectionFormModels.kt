package com.sivemore.mobile.feature.inspection

data class InspectionQuestionOption(
    val id: String,
    val label: String,
)

enum class InspectionQuestionKind {
    SingleChoice,
    NumericInput,
}

enum class InspectionIllustrationType {
    Birlos,
    Tuercas,
}

data class BirlosVisualState(
    val count: Int,
    val birlosState: List<Boolean>,
    val evaluated: List<Boolean>,
) {
    val isComplete: Boolean
        get() = evaluated.size == count && evaluated.all { it }
}

data class InspectionQuestionGroup(
    val id: String,
    val title: String,
    val illustrationType: InspectionIllustrationType? = null,
    val birlosVisualState: BirlosVisualState? = null,
    val questions: List<InspectionQuestionItem>,
)

data class InspectionQuestionItem(
    val id: String,
    val title: String,
    val kind: InspectionQuestionKind = InspectionQuestionKind.SingleChoice,
    val options: List<InspectionQuestionOption> = emptyList(),
    val selectedOptionId: String? = null,
    val helperText: String? = null,
    val placeholder: String = "Ingresa numero",
    val numericValue: String = "",
) {
    val isComplete: Boolean
        get() = when (kind) {
            InspectionQuestionKind.SingleChoice -> selectedOptionId != null
            InspectionQuestionKind.NumericInput -> numericValue.isNotBlank()
        }
}

data class InspectionSectionUiState(
    val id: String,
    val title: String,
    val description: String,
    val questions: List<InspectionQuestionItem> = emptyList(),
    val groups: List<InspectionQuestionGroup> = emptyList(),
) {
    val allQuestions: List<InspectionQuestionItem>
        get() = if (groups.isNotEmpty()) groups.flatMap { it.questions } else questions

    val isComplete: Boolean
        get() = allQuestions.all { it.isComplete } && groups.all { it.birlosVisualState?.isComplete ?: true }
}

object InspectionSectionCatalog {
    private const val DefaultBirlosCount = 6

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

    private val rimOptions = listOf(
        InspectionQuestionOption(id = "APPROVED", label = "Aprobados"),
        InspectionQuestionOption(id = "LEFT_DAMAGED", label = "Izquierdo roto o soldado"),
        InspectionQuestionOption(id = "RIGHT_DAMAGED", label = "Derecho roto o soldado"),
        InspectionQuestionOption(id = "BOTH_DAMAGED", label = "Ambos rotos o soldados"),
    )

    private val massOptions = listOf(
        InspectionQuestionOption(id = "APPROVED", label = "Aprobadas"),
        InspectionQuestionOption(id = "LEFT_LEAK", label = "Izquierda con fuga"),
        InspectionQuestionOption(id = "RIGHT_LEAK", label = "Derecha con fuga"),
        InspectionQuestionOption(id = "BOTH_LEAK", label = "Ambas con fuga"),
    )

    private val tuercasOptions = listOf(
        InspectionQuestionOption(id = "APPROVED", label = "Aprobados"),
        InspectionQuestionOption(id = "BROKEN", label = "Rotos"),
        InspectionQuestionOption(id = "MISSING", label = "Faltantes"),
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

    fun llantasSection(): InspectionSectionUiState = InspectionSectionUiState(
        id = "llantas",
        title = "Llantas",
        description = "Completa la inspeccion de llantas antes de avanzar a la siguiente seccion.",
        groups = listOf(
            InspectionQuestionGroup(
                id = "llantas_general",
                title = "Llantas",
                questions = listOf(
                    InspectionQuestionItem(id = "llantas_rines_delanteros", title = "Llantas rines delanteros", options = rimOptions),
                    InspectionQuestionItem(id = "llantas_rines_traseros", title = "Llantas rines traseros", options = rimOptions),
                    InspectionQuestionItem(id = "llantas_masas_delanteras", title = "Llantas masas delanteras", options = massOptions),
                    InspectionQuestionItem(id = "llantas_masas_traseras", title = "Llantas masas traseras", options = massOptions),
                ),
            ),
            InspectionQuestionGroup(
                id = "llantas_presion",
                title = "Presion",
                questions = listOf(
                    numericQuestion("llantas_presion_delantera_izquierda", "Llantas presion delantera izquierda", "Ingresa el valor en PSI"),
                    numericQuestion("llantas_presion_delantera_derecha", "Llantas presion delantera derecha", "Ingresa el valor en PSI"),
                    numericQuestion("llantas_presion_trasera_izquierda_1", "Llantas presion trasera izquierda 1", "Ingresa el valor en PSI"),
                    numericQuestion("llantas_presion_trasera_izquierda_2", "Llantas presion trasera izquierda 2", "Ingresa el valor en PSI"),
                    numericQuestion("llantas_presion_trasera_derecha_1", "Llantas presion trasera derecha 1", "Ingresa el valor en PSI"),
                    numericQuestion("llantas_presion_trasera_derecha_2", "Llantas presion trasera derecha 2", "Ingresa el valor en PSI"),
                ),
            ),
            InspectionQuestionGroup(
                id = "llantas_profundidad",
                title = "Profundidad",
                questions = listOf(
                    numericQuestion("llantas_profundidad_delantera_izquierda", "Llantas profundidad delantera izquierda", "Ingresa el valor en mm:"),
                    numericQuestion("llantas_profundidad_delantera_derecha", "Llantas profundidad delantera derecha", "Ingresa el valor en mm:"),
                    numericQuestion("llantas_profundidad_trasera_izquierda_1", "Llantas profundidad trasera izquierda 1", "Ingresa el valor en mm:"),
                    numericQuestion("llantas_profundidad_trasera_izquierda_2", "Llantas profundidad trasera izquierda 2", "Ingresa el valor en mm:"),
                    numericQuestion("llantas_profundidad_trasera_derecha_1", "Llantas profundidad trasera derecha 1", "Ingresa el valor en mm:"),
                    numericQuestion("llantas_profundidad_trasera_derecha_2", "Llantas profundidad trasera derecha 2", "Ingresa el valor en mm:"),
                ),
            ),
            InspectionQuestionGroup(
                id = "llantas_birlos",
                title = "Birlos",
                illustrationType = InspectionIllustrationType.Birlos,
                birlosVisualState = BirlosVisualState(
                    count = DefaultBirlosCount,
                    birlosState = List(DefaultBirlosCount) { false },
                    evaluated = List(DefaultBirlosCount) { false },
                ),
                questions = emptyList(),
            ),
            InspectionQuestionGroup(
                id = "llantas_tuercas",
                title = "Tuercas",
                illustrationType = InspectionIllustrationType.Tuercas,
                questions = listOf(
                    InspectionQuestionItem(id = "llantas_tuercas_delantera_izquierda", title = "Llantas tuercas delantera izquierda", options = tuercasOptions),
                    numericQuestion("llantas_tuercas_faltantes_delantera_izquierda", "Tuercas faltantes delantera izquierda", "Ingresa la cantidad de tuercas faltantes:"),
                    numericQuestion("llantas_tuercas_rotas_delantera_izquierda", "Tuercas rotas delantera izquierda", "Ingresa la cantidad de tuercas rotas:"),
                    InspectionQuestionItem(id = "llantas_tuercas_delantera_derecha", title = "Llantas tuercas delantera derecha", options = tuercasOptions),
                    numericQuestion("llantas_tuercas_faltantes_delantera_derecha", "Tuercas faltantes delantera derecha", "Ingresa la cantidad de tuercas faltantes:"),
                    numericQuestion("llantas_tuercas_rotas_delantera_derecha", "Tuercas rotas delantera derecha", "Ingresa la cantidad de tuercas rotas:"),
                    InspectionQuestionItem(id = "llantas_tuercas_trasera_izquierda", title = "Llantas tuercas trasera izquierda", options = tuercasOptions),
                    numericQuestion("llantas_tuercas_faltantes_trasera_izquierda", "Tuercas faltantes trasera izquierda", "Ingresa la cantidad de tuercas faltantes:"),
                    numericQuestion("llantas_tuercas_rotas_trasera_izquierda", "Tuercas rotas trasera izquierda", "Ingresa la cantidad de tuercas rotas:"),
                    InspectionQuestionItem(id = "llantas_tuercas_trasera_derecha", title = "Llantas tuercas trasera derecha", options = tuercasOptions),
                    numericQuestion("llantas_tuercas_faltantes_trasera_derecha", "Tuercas faltantes trasera derecha", "Ingresa la cantidad de tuercas faltantes:"),
                    numericQuestion("llantas_tuercas_rotas_trasera_derecha", "Tuercas rotas trasera derecha", "Ingresa la cantidad de tuercas rotas:"),
                ),
            ),
        ),
    )

    private fun numericQuestion(
        id: String,
        title: String,
        helperText: String,
    ) = InspectionQuestionItem(
        id = id,
        title = title,
        kind = InspectionQuestionKind.NumericInput,
        helperText = helperText,
    )
}
