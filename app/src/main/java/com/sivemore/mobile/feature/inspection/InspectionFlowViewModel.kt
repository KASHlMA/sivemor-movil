package com.sivemore.mobile.feature.inspection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivemore.mobile.data.network.MobileEvaluacionRequestDto
import com.sivemore.mobile.domain.model.EvidenceUpload
import com.sivemore.mobile.domain.model.InspectionFlowAnswerDraft
import com.sivemore.mobile.domain.model.InspectionSection
import com.sivemore.mobile.domain.model.VerificationSession
import com.sivemore.mobile.domain.repository.AuthRepository
import com.sivemore.mobile.domain.repository.VerificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class InspectionFlowViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val verificationRepository: VerificationRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val vehicleId: String = checkNotNull(savedStateHandle["vehicleId"])

    private val _uiState = MutableStateFlow(
        InspectionFlowUiState(
            vehicleId = vehicleId,
            lucesSection = InspectionSectionCatalog.lucesSection(),
            llantasSection = InspectionSectionCatalog.llantasSection(),
            direccionSection = InspectionSectionCatalog.direccionSection(),
            aireFrenosSection = InspectionSectionCatalog.aireFrenosSection(),
            motorEmisionesSection = InspectionSectionCatalog.motorEmisionesSection(),
            otrosSection = InspectionSectionCatalog.otrosSection(),
        ),
    )
    val uiState: StateFlow<InspectionFlowUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<InspectionFlowEvent>()
    val events: SharedFlow<InspectionFlowEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun onAction(action: InspectionFlowAction) {
        when (action) {
            is InspectionFlowAction.EnterSection -> _uiState.update {
                it.copy(currentSectionIndex = action.index, errorMessage = null)
            }
            is InspectionFlowAction.LucesOptionSelected -> updateSingleChoiceAnswer("luces", action.questionId, action.optionId)
            is InspectionFlowAction.LlantasOptionSelected -> updateSingleChoiceAnswer("llantas", action.questionId, action.optionId)
            is InspectionFlowAction.LlantasNumericValueChanged -> updateNumericAnswer(action.questionId, action.value, "llantas")
            is InspectionFlowAction.DireccionOptionSelected -> updateSingleChoiceAnswer("direccion", action.questionId, action.optionId)
            is InspectionFlowAction.DireccionNumericValueChanged -> updateNumericAnswer(action.questionId, action.value, "direccion")
            is InspectionFlowAction.AireFrenosOptionSelected -> updateSingleChoiceAnswer("aire_frenos", action.questionId, action.optionId)
            is InspectionFlowAction.AireFrenosNumericValueChanged -> updateNumericAnswer(action.questionId, action.value, "aire_frenos")
            is InspectionFlowAction.MotorEmisionesOptionSelected -> updateSingleChoiceAnswer("motor_emisiones", action.questionId, action.optionId)
            is InspectionFlowAction.OtrosOptionSelected -> updateSingleChoiceAnswer("otros", action.questionId, action.optionId)
            is InspectionFlowAction.BirloToggled -> updateBirloState(action.groupId, action.birloIndex, action.checked)
            is InspectionFlowAction.BirlosCountChanged -> updateBirlosCount(action.groupId, action.value)
            InspectionFlowAction.PreviousClicked -> moveToPreviousSection()
            InspectionFlowAction.NextClicked -> navigateNextIfComplete()
            InspectionFlowAction.CommentDialogOpened -> _uiState.update { it.copy(showCommentDialog = true, errorMessage = null) }
            InspectionFlowAction.CommentDialogDismissed -> _uiState.update { it.copy(showCommentDialog = false) }
            is InspectionFlowAction.CommentDraftChanged -> _uiState.update { it.copy(commentDraft = action.value, errorMessage = null) }
            InspectionFlowAction.CommentSaved -> saveComment()
            is InspectionFlowAction.EvidencePicked -> addEvidence(action.upload)
            is InspectionFlowAction.RemoveEvidence -> mutate {
                verificationRepository.removeEvidence(vehicleId, action.evidenceId)
            }
            InspectionFlowAction.PauseRequested -> _uiState.update { it.copy(showPauseDialog = true, errorMessage = null) }
            InspectionFlowAction.PauseDismissed -> _uiState.update { it.copy(showPauseDialog = false) }
            InspectionFlowAction.PauseConfirmed -> pauseVerification()
            InspectionFlowAction.SubmitRequested -> completeVerification()
            InspectionFlowAction.LogoutRequested -> signOut()
            InspectionFlowAction.PhotoLimitReached -> _uiState.update {
                it.copy(errorMessage = "Se alcanzo el numero maximo de fotos permitidas (3).")
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            runCatching {
                verificationRepository.loadSession(vehicleId)
            }.onSuccess { session ->
                _uiState.update {
                    it.copy(
                        session = session,
                        commentDraft = session.comments,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = failure.message ?: "No fue posible cargar la inspeccion.",
                    )
                }
            }
        }
    }

    private fun updateSingleChoiceAnswer(
        sectionId: String,
        questionId: String,
        optionId: String,
    ) {
        _uiState.update { current ->
            current.copy(
                lucesSection = if (sectionId == "luces") {
                    current.lucesSection.updateQuestion(questionId) { it.copy(selectedOptionId = optionId) }
                } else {
                    current.lucesSection
                },
                llantasSection = if (sectionId == "llantas") {
                    current.llantasSection.updateQuestion(questionId) { it.copy(selectedOptionId = optionId) }
                } else {
                    current.llantasSection
                },
                direccionSection = if (sectionId == "direccion") {
                    current.direccionSection.updateQuestion(questionId) { it.copy(selectedOptionId = optionId) }
                } else {
                    current.direccionSection
                },
                aireFrenosSection = if (sectionId == "aire_frenos") {
                    current.aireFrenosSection.updateQuestion(questionId) { it.copy(selectedOptionId = optionId) }
                } else {
                    current.aireFrenosSection
                },
                motorEmisionesSection = if (sectionId == "motor_emisiones") {
                    current.motorEmisionesSection.updateQuestion(questionId) { it.copy(selectedOptionId = optionId) }
                } else {
                    current.motorEmisionesSection
                },
                otrosSection = if (sectionId == "otros") {
                    current.otrosSection.updateQuestion(questionId) { it.copy(selectedOptionId = optionId) }
                } else {
                    current.otrosSection
                },
                errorMessage = null,
            )
        }
    }

    private fun updateNumericAnswer(
        questionId: String,
        value: String,
        sectionId: String,
    ) {
        val sanitizedValue = sanitizeDecimalInput(value)
        _uiState.update { current ->
            current.copy(
                llantasSection = if (sectionId == "llantas") {
                    current.llantasSection.updateQuestion(questionId) { it.copy(numericValue = sanitizedValue) }
                } else {
                    current.llantasSection
                },
                direccionSection = if (sectionId == "direccion") {
                    current.direccionSection.updateQuestion(questionId) { it.copy(numericValue = sanitizedValue) }
                } else {
                    current.direccionSection
                },
                aireFrenosSection = if (sectionId == "aire_frenos") {
                    current.aireFrenosSection.updateQuestion(questionId) { it.copy(numericValue = sanitizedValue) }
                } else {
                    current.aireFrenosSection
                },
                errorMessage = null,
            )
        }
    }

    private fun updateBirloState(
        groupId: String,
        birloIndex: Int,
        checked: Boolean,
    ) {
        _uiState.update { current ->
            current.copy(
                llantasSection = current.llantasSection.updateBirlosGroup(groupId, birloIndex, checked),
                errorMessage = null,
            )
        }
    }

    private fun updateBirlosCount(
        groupId: String,
        value: String,
    ) {
        val parsed = value.toIntOrNull()?.coerceIn(0, 8) ?: 0
        _uiState.update { current ->
            current.copy(
                llantasSection = current.llantasSection.updateBirlosCount(groupId, parsed),
                errorMessage = null,
            )
        }
    }

    private fun moveToPreviousSection() {
        _uiState.update { current ->
            if (current.currentSectionIndex == 0) current else current.copy(
                currentSectionIndex = current.currentSectionIndex - 1,
                errorMessage = null,
            )
        }
    }

    private fun navigateNextIfComplete() {
        if (!uiState.value.isCurrentSectionComplete) return
        viewModelScope.launch {
            _uiState.update { it.copy(currentSectionIndex = it.currentSectionIndex + 1, errorMessage = null) }
            _events.emit(InspectionFlowEvent.NavigateToNextSection)
        }
    }

    private fun addEvidence(upload: EvidenceUpload) {
        val currentSectionId = uiState.value.currentSection?.id ?: uiState.value.session?.sections?.lastOrNull()?.id
        if (currentSectionId == null) {
            _uiState.update { it.copy(errorMessage = "No hay una seccion activa para agregar evidencia.") }
            return
        }
        if (!uiState.value.canAddMorePhotos) {
            _uiState.update { it.copy(errorMessage = "Se alcanzo el numero maximo de fotos permitidas (3).") }
            return
        }
        viewModelScope.launch {
            val previousSession = uiState.value.session
            runCatching {
                verificationRepository.addEvidence(vehicleId, currentSectionId, upload)
            }.onSuccess { session ->
                _uiState.update {
                    it.copy(
                        session = preserveEvidencePreviewUris(
                            previous = previousSession,
                            updated = session,
                            newEvidenceSectionId = currentSectionId,
                            upload = upload,
                        ),
                        errorMessage = null,
                    )
                }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(errorMessage = failure.message ?: "No fue posible actualizar la inspeccion.")
                }
            }
        }
    }

    private fun mutate(
        mutation: suspend () -> VerificationSession,
    ) {
        viewModelScope.launch {
            runCatching { mutation() }
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(
                            session = preserveEvidencePreviewUris(
                                previous = it.session,
                                updated = session,
                            ),
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { failure ->
                    _uiState.update { it.copy(errorMessage = failure.message ?: "No fue posible actualizar la inspeccion.") }
                }
        }
    }

    private suspend fun persistCommentIfNeeded() {
        val state = uiState.value
        val session = state.session ?: return
        if (session.comments == state.commentDraft) return
        _uiState.update { it.copy(isSavingComment = true) }
        val updated = verificationRepository.updateComments(vehicleId, state.commentDraft)
        _uiState.update {
            it.copy(
                session = updated,
                commentDraft = updated.comments,
                isSavingComment = false,
            )
        }
    }

    private fun saveComment() {
        viewModelScope.launch {
            runCatching { persistCommentIfNeeded() }
                .onSuccess { _uiState.update { it.copy(showCommentDialog = false, errorMessage = null) } }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(
                            showCommentDialog = true,
                            isSavingComment = false,
                            errorMessage = failure.message ?: "No fue posible guardar el comentario.",
                        )
                    }
                }
        }
    }

    private fun pauseVerification() {
        viewModelScope.launch {
            runCatching {
                persistCommentIfNeeded()
                verificationRepository.pauseSession(vehicleId)
            }.onSuccess {
                _uiState.update { it.copy(showPauseDialog = false, isSavingComment = false) }
                _events.emit(InspectionFlowEvent.BackToMenu)
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(
                        showPauseDialog = false,
                        isSavingComment = false,
                        errorMessage = failure.message ?: "No fue posible pausar la verificacion.",
                    )
                }
            }
        }
    }

    private fun completeVerification() {
        if (!uiState.value.isEntireVerificationComplete) {
            _uiState.update {
                it.copy(
                    errorMessage = "Debes completar todas las secciones antes de finalizar la verificacion."
                )
            }
            return
        }
        viewModelScope.launch {
            runCatching {
                val state = uiState.value
                verificationRepository.syncInspectionFlowDraft(
                    orderUnitId = vehicleId,
                    overallComment = state.commentDraft,
                    answers = state.toInspectionFlowAnswers(),
                )
                verificationRepository.completeSession(vehicleId)
                val inspectionId = state.session?.id?.toLongOrNull()
                if (inspectionId != null) {
                    verificationRepository.submitEvaluacion(state.toEvaluacionRequest(inspectionId))
                }
            }.onSuccess {
                _uiState.update { it.copy(isSavingComment = false) }
                _events.emit(InspectionFlowEvent.Completed)
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(
                        isSavingComment = false,
                        errorMessage = failure.message ?: "No fue posible enviar la inspeccion.",
                    )
                }
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _events.emit(InspectionFlowEvent.SignedOut)
        }
    }
}

private fun preserveEvidencePreviewUris(
    previous: VerificationSession?,
    updated: VerificationSession,
    newEvidenceSectionId: String? = null,
    upload: EvidenceUpload? = null,
): VerificationSession {
    val preservedUris = previous
        ?.sections
        .orEmpty()
        .flatMap { section -> section.evidence }
        .associate { evidence -> evidence.id to evidence.previewUri }

    val mergedSections = updated.sections.map { section ->
        section.copy(
            evidence = section.evidence.map { evidence ->
                evidence.copy(previewUri = preservedUris[evidence.id])
            },
        )
    }

    val withNewEvidenceUri = if (newEvidenceSectionId == null || upload?.fileName == null) {
        mergedSections
    } else {
        mergedSections.map { section ->
            if (section.id != newEvidenceSectionId) {
                section
            } else {
                section.copy(
                    evidence = applyPreviewUriToNewEvidence(
                        evidence = section.evidence,
                        fileName = upload.fileName,
                        previewUri = upload.uri,
                    ),
                )
            }
        }
    }

    return updated.copy(sections = withNewEvidenceUri)
}

private fun applyPreviewUriToNewEvidence(
    evidence: List<com.sivemore.mobile.domain.model.EvidenceItem>,
    fileName: String,
    previewUri: String,
): List<com.sivemore.mobile.domain.model.EvidenceItem> {
    var applied = false
    return evidence.map { item ->
        if (!applied && item.title == fileName && item.previewUri == null) {
            applied = true
            item.copy(previewUri = previewUri)
        } else {
            item
        }
    }
}

private fun InspectionFlowUiState.toEvaluacionRequest(inspectionId: Long): MobileEvaluacionRequestDto {
    fun q(sectionId: String, questionId: String): String? = when (sectionId) {
        "luces" -> lucesSection
        "llantas" -> llantasSection
        "direccion" -> direccionSection
        "aire_frenos" -> aireFrenosSection
        "motor_emisiones" -> motorEmisionesSection
        "otros" -> otrosSection
        else -> null
    }?.allQuestions?.firstOrNull { it.id == questionId }?.selectedOptionId

    fun n(sectionId: String, questionId: String): Double? = when (sectionId) {
        "llantas" -> llantasSection
        "direccion" -> direccionSection
        "aire_frenos" -> aireFrenosSection
        else -> null
    }?.allQuestions?.firstOrNull { it.id == questionId }?.numericValue?.toDoubleOrNull()

    fun ni(sectionId: String, questionId: String): Int? = when (sectionId) {
        "llantas" -> llantasSection
        "direccion" -> direccionSection
        else -> null
    }?.allQuestions?.firstOrNull { it.id == questionId }?.numericValue?.toIntOrNull()

    fun birlosSelected(groupId: String): String? = llantasSection.groups
        .firstOrNull { it.id == groupId }?.birlosVisualState?.birlosState
        ?.mapIndexed { i, v -> if (v) i.toString() else null }
        ?.filterNotNull()?.joinToString(",")

    fun birlosCount(groupId: String): Int? = llantasSection.groups
        .firstOrNull { it.id == groupId }?.birlosVisualState?.count

    return MobileEvaluacionRequestDto(
        inspectionId = inspectionId,
        lucesGalibo = q("luces", "luces_galibo"),
        lucesAltas = q("luces", "luces_altas"),
        lucesBajas = q("luces", "luces_bajas"),
        lucesDemarcadorasDelanteras = q("luces", "luces_demarcadoras_delanteras"),
        lucesDemarcadorasTraseras = q("luces", "luces_demarcadoras_traseras"),
        lucesIndicadoras = q("luces", "luces_indicadoras"),
        faroIzquierdo = q("luces", "faro_izquierdo"),
        faroDerecho = q("luces", "faro_derecho"),
        lucesDireccionalesDelanteras = q("luces", "luces_direccionales_delanteras"),
        lucesDireccionalesTraseras = q("luces", "luces_direccionales_traseras"),
        llantasRinesDelanteros = q("llantas", "llantas_rines_delanteros"),
        llantasRinesTraseros = q("llantas", "llantas_rines_traseros"),
        llantasMasasDelanteras = q("llantas", "llantas_masas_delanteras"),
        llantasMasasTraseras = q("llantas", "llantas_masas_traseras"),
        llantasPresionDelanteraIzquierda = n("llantas", "llantas_presion_delantera_izquierda"),
        llantasPresionDelanteraDerecha = n("llantas", "llantas_presion_delantera_derecha"),
        llantasPresionTraseraIzquierda1 = n("llantas", "llantas_presion_trasera_izquierda_1"),
        llantasPresionTraseraIzquierda2 = n("llantas", "llantas_presion_trasera_izquierda_2"),
        llantasPresionTraseraDerecha1 = n("llantas", "llantas_presion_trasera_derecha_1"),
        llantasPresionTraseraDerecha2 = n("llantas", "llantas_presion_trasera_derecha_2"),
        llantasProfundidadDelanteraIzquierda = n("llantas", "llantas_profundidad_delantera_izquierda"),
        llantasProfundidadDelanteraDerecha = n("llantas", "llantas_profundidad_delantera_derecha"),
        llantasProfundidadTraseraIzquierda1 = n("llantas", "llantas_profundidad_trasera_izquierda_1"),
        llantasProfundidadTraseraIzquierda2 = n("llantas", "llantas_profundidad_trasera_izquierda_2"),
        llantasProfundidadTraseraDerecha1 = n("llantas", "llantas_profundidad_trasera_derecha_1"),
        llantasProfundidadTraseraDerecha2 = n("llantas", "llantas_profundidad_trasera_derecha_2"),
        llantasTuercasDelanteraIzquierda = q("llantas", "llantas_tuercas_delantera_izquierda"),
        llantasTuercasDelanteraIzquierdaFaltantes = ni("llantas", "llantas_tuercas_faltantes_delantera_izquierda"),
        llantasTuercasDelanteraIzquierdaRotas = ni("llantas", "llantas_tuercas_rotas_delantera_izquierda"),
        llantasTuercasDelanteraDerecha = q("llantas", "llantas_tuercas_delantera_derecha"),
        llantasTuercasDelanteraDerechaFaltantes = ni("llantas", "llantas_tuercas_faltantes_delantera_derecha"),
        llantasTuercasDelanteraDerechaRotas = ni("llantas", "llantas_tuercas_rotas_delantera_derecha"),
        llantasTuercasTraseraIzquierda = q("llantas", "llantas_tuercas_trasera_izquierda"),
        llantasTuercasTraseraIzquierdaFaltantes = ni("llantas", "llantas_tuercas_faltantes_trasera_izquierda"),
        llantasTuercasTraseraIzquierdaRotas = ni("llantas", "llantas_tuercas_rotas_trasera_izquierda"),
        llantasTuercasTraseraDerecha = q("llantas", "llantas_tuercas_trasera_derecha"),
        llantasTuercasTraseraDerechaFaltantes = ni("llantas", "llantas_tuercas_faltantes_trasera_derecha"),
        llantasTuercasTraseraDerechaRotas = ni("llantas", "llantas_tuercas_rotas_trasera_derecha"),
        llantasBirlosDelanteraIzquierdaCount = birlosCount("llantas_birlos_delantera_izquierda"),
        llantasBirlosDelanteraIzquierdaSelected = birlosSelected("llantas_birlos_delantera_izquierda"),
        llantasBirlosDelanteraDerechaCount = birlosCount("llantas_birlos_delantera_derecha"),
        llantasBirlosDelanteraDerechaSelected = birlosSelected("llantas_birlos_delantera_derecha"),
        llantasBirlosTraseraIzquierdaCount = birlosCount("llantas_birlos_trasera_izquierda"),
        llantasBirlosTraseraIzquierdaSelected = birlosSelected("llantas_birlos_trasera_izquierda"),
        llantasBirlosTraseraDerechaCount = birlosCount("llantas_birlos_trasera_derecha"),
        llantasBirlosTraseraDerechaSelected = birlosSelected("llantas_birlos_trasera_derecha"),
        llantasBirlosMediaIzquierdaCount = birlosCount("llantas_birlos_media_izquierda"),
        llantasBirlosMediaIzquierdaSelected = birlosSelected("llantas_birlos_media_izquierda"),
        llantasBirlosMediaDerechaCount = birlosCount("llantas_birlos_media_derecha"),
        llantasBirlosMediaDerechaSelected = birlosSelected("llantas_birlos_media_derecha"),
        direccionBrazoPitman = q("direccion", "direccion_brazo_pitman"),
        direccionManijasPuertas = q("direccion", "direccion_manijas_puertas"),
        direccionChavetas = q("direccion", "direccion_chavetas"),
        direccionChavetasFaltantes = ni("direccion", "direccion_chavetas_faltantes"),
        aireFrenosCompresor = q("aire_frenos", "aire_frenos_compresor"),
        aireFrenosTanquesAire = q("aire_frenos", "aire_frenos_tanques_aire"),
        aireFrenosTiempoCargaPsi = n("aire_frenos", "aire_frenos_tiempo_carga_psi"),
        aireFrenosTiempoCargaTiempo = n("aire_frenos", "aire_frenos_tiempo_carga_tiempo"),
        motorEmisionesHumo = q("motor_emisiones", "motor_emisiones_humo"),
        motorEmisionesGobernado = q("motor_emisiones", "motor_emisiones_gobernado"),
        otrosCajaDireccion = q("otros", "otros_caja_direccion"),
        otrosDepositoAceite = q("otros", "otros_deposito_aceite"),
        otrosParabrisas = q("otros", "otros_parabrisas"),
        otrosLimpiaparabrisas = q("otros", "otros_limpiaparabrisas"),
        otrosJuego = q("otros", "otros_juego"),
        otrosEscape = q("otros", "otros_escape"),
        comentariosGenerales = commentDraft.ifBlank { null },
    )
}

private fun InspectionFlowUiState.toInspectionFlowAnswers(): List<InspectionFlowAnswerDraft> = buildList {
    addAll(lucesSection.toAnswerDrafts())
    addAll(llantasSection.toAnswerDrafts())
    addAll(direccionSection.toAnswerDrafts())
    addAll(aireFrenosSection.toAnswerDrafts())
    addAll(motorEmisionesSection.toAnswerDrafts())
    addAll(otrosSection.toAnswerDrafts())
}

private fun InspectionSectionUiState.toAnswerDrafts(): List<InspectionFlowAnswerDraft> = allQuestions.mapNotNull { question ->
    when (question.kind) {
        InspectionQuestionKind.SingleChoice -> question.selectedOptionId?.let { selected ->
            InspectionFlowAnswerDraft(
                questionCode = question.id,
                answer = selected.toBackendAnswer(),
                comment = "",
            )
        }

        InspectionQuestionKind.NumericInput -> question.numericValue
            .takeIf { it.isNotBlank() }
            ?.let { value ->
                InspectionFlowAnswerDraft(
                    questionCode = question.id,
                    answer = question.toNumericBackendAnswer(),
                    comment = value,
                )
            }
    }
}

private fun String.toBackendAnswer(): String = when (this) {
    "APPROVED" -> "PASS"
    else -> "FAIL"
}

internal fun sanitizeDecimalInput(value: String): String {
    val trimmed = value.trim()
    val builder = StringBuilder(trimmed.length)
    var hasDecimalSeparator = false

    trimmed.forEachIndexed { index, char ->
        when {
            char.isDigit() -> builder.append(char)
            char == '.' && !hasDecimalSeparator && builder.isNotEmpty() -> {
                builder.append(char)
                hasDecimalSeparator = true
            }
            char == '.' && !hasDecimalSeparator && builder.isEmpty() && index == 0 -> {
                builder.append("0.")
                hasDecimalSeparator = true
            }
        }
    }

    return builder.toString()
}

private fun InspectionQuestionItem.toNumericBackendAnswer(): String {
    val numericValue = numericValue.replace(",", ".").toDoubleOrNull() ?: return "PASS"
    val passes = when (id) {
        "llantas_presion_delantera_izquierda",
        "llantas_presion_delantera_derecha",
        "llantas_presion_trasera_izquierda_1",
        "llantas_presion_trasera_izquierda_2",
        "llantas_presion_trasera_derecha_1",
        "llantas_presion_trasera_derecha_2" -> numericValue >= 80.0

        "llantas_profundidad_delantera_izquierda",
        "llantas_profundidad_delantera_derecha" -> numericValue >= 3.2

        "llantas_profundidad_trasera_izquierda_1",
        "llantas_profundidad_trasera_izquierda_2",
        "llantas_profundidad_trasera_derecha_1",
        "llantas_profundidad_trasera_derecha_2" -> numericValue >= 1.6

        "aire_frenos_tiempo_carga_psi" -> numericValue in 70.0..120.0
        "aire_frenos_tiempo_carga_tiempo" -> numericValue < 120.0
        else -> true
    }
    return if (passes) "PASS" else "FAIL"
}

data class InspectionFlowUiState(
    val vehicleId: String,
    val isLoading: Boolean = true,
    val lucesSection: InspectionSectionUiState,
    val llantasSection: InspectionSectionUiState,
    val direccionSection: InspectionSectionUiState,
    val aireFrenosSection: InspectionSectionUiState,
    val motorEmisionesSection: InspectionSectionUiState,
    val otrosSection: InspectionSectionUiState,
    val session: VerificationSession? = null,
    val currentSectionIndex: Int = 0,
    val commentDraft: String = "",
    val isSavingComment: Boolean = false,
    val showCommentDialog: Boolean = false,
    val showPauseDialog: Boolean = false,
    val errorMessage: String? = null,
) {
    val currentSection: InspectionSection?
        get() = session?.sections?.getOrNull(currentSectionIndex)

    val currentEvidenceCount: Int
        get() = currentSection?.evidence?.size ?: 0

    val canAddMorePhotos: Boolean
        get() = totalEvidenceCount < 3

    val allEvidence: List<com.sivemore.mobile.domain.model.EvidenceItem>
        get() = session?.sections.orEmpty().flatMap { it.evidence }

    val totalEvidenceCount: Int
        get() = allEvidence.size

    val canGoBack: Boolean
        get() = currentSectionIndex > 0

    val isCurrentSectionComplete: Boolean
        get() = when (currentSectionIndex) {
            0 -> lucesSection.isComplete
            1 -> llantasSection.isComplete
            2 -> direccionSection.isComplete
            3 -> aireFrenosSection.isComplete
            4 -> motorEmisionesSection.isComplete
            5 -> otrosSection.isComplete
            else -> currentSection?.items.orEmpty().all { !it.required || it.selectedOptionId != null }
        }

    val isEntireVerificationComplete: Boolean
        get() {
            val remainingSectionsComplete = session?.sections
                ?.drop(6)
                .orEmpty()
                .all { section -> section.items.all { !it.required || it.selectedOptionId != null } }
            return lucesSection.isComplete &&
                llantasSection.isComplete &&
                direccionSection.isComplete &&
                aireFrenosSection.isComplete &&
                motorEmisionesSection.isComplete &&
                otrosSection.isComplete &&
                remainingSectionsComplete
        }
}

sealed interface InspectionFlowAction {
    data class EnterSection(val index: Int) : InspectionFlowAction
    data class LucesOptionSelected(val questionId: String, val optionId: String) : InspectionFlowAction
    data class LlantasOptionSelected(val questionId: String, val optionId: String) : InspectionFlowAction
    data class LlantasNumericValueChanged(val questionId: String, val value: String) : InspectionFlowAction
    data class DireccionOptionSelected(val questionId: String, val optionId: String) : InspectionFlowAction
    data class DireccionNumericValueChanged(val questionId: String, val value: String) : InspectionFlowAction
    data class AireFrenosOptionSelected(val questionId: String, val optionId: String) : InspectionFlowAction
    data class AireFrenosNumericValueChanged(val questionId: String, val value: String) : InspectionFlowAction
    data class MotorEmisionesOptionSelected(val questionId: String, val optionId: String) : InspectionFlowAction
    data class OtrosOptionSelected(val questionId: String, val optionId: String) : InspectionFlowAction
    data class BirloToggled(val groupId: String, val birloIndex: Int, val checked: Boolean) : InspectionFlowAction
    data class BirlosCountChanged(val groupId: String, val value: String) : InspectionFlowAction
    data object PreviousClicked : InspectionFlowAction
    data object CommentDialogOpened : InspectionFlowAction
    data object CommentDialogDismissed : InspectionFlowAction
    data class CommentDraftChanged(val value: String) : InspectionFlowAction
    data object CommentSaved : InspectionFlowAction
    data class EvidencePicked(val upload: EvidenceUpload) : InspectionFlowAction
    data class RemoveEvidence(val evidenceId: String) : InspectionFlowAction
    data object NextClicked : InspectionFlowAction
    data object PauseRequested : InspectionFlowAction
    data object PauseDismissed : InspectionFlowAction
    data object PauseConfirmed : InspectionFlowAction
    data object SubmitRequested : InspectionFlowAction
    data object LogoutRequested : InspectionFlowAction
    data object PhotoLimitReached : InspectionFlowAction
}

sealed interface InspectionFlowEvent {
    data object NavigateToNextSection : InspectionFlowEvent
    data object BackToLookup : InspectionFlowEvent
    data object BackToMenu : InspectionFlowEvent
    data object Completed : InspectionFlowEvent
    data object SignedOut : InspectionFlowEvent
}

private fun InspectionSectionUiState.updateQuestion(
    questionId: String,
    transform: (InspectionQuestionItem) -> InspectionQuestionItem,
): InspectionSectionUiState = if (groups.isNotEmpty()) {
    copy(
        groups = groups.map { group ->
            group.copy(
                questions = group.questions.map { question ->
                    if (question.id == questionId) transform(question) else question
                },
            )
        },
    )
} else {
    copy(
        questions = questions.map { question ->
            if (question.id == questionId) transform(question) else question
        },
    )
}

private fun InspectionSectionUiState.updateBirlosGroup(
    groupId: String,
    birloIndex: Int,
    checked: Boolean,
): InspectionSectionUiState = copy(
    groups = groups.map { group ->
        if (group.id != groupId || group.birlosVisualState == null) {
            group
        } else {
            val birlosState = group.birlosVisualState.birlosState.toMutableList()
            val evaluated = group.birlosVisualState.evaluated.toMutableList()
            if (birloIndex in birlosState.indices) {
                birlosState[birloIndex] = checked
                evaluated[birloIndex] = true
            }
            group.copy(
                birlosVisualState = group.birlosVisualState.copy(
                    birlosState = birlosState,
                    evaluated = evaluated,
                ),
            )
        }
    },
)

private fun InspectionSectionUiState.updateBirlosCount(
    groupId: String,
    count: Int,
): InspectionSectionUiState = copy(
    groups = groups.map { group ->
        if (group.id != groupId || group.birlosVisualState == null) {
            group
        } else {
            val currentState = group.birlosVisualState
            val resizedBirlosState = List(count) { index -> currentState.birlosState.getOrElse(index) { false } }
            val resizedEvaluated = List(count) { index -> currentState.evaluated.getOrElse(index) { false } }
            group.copy(
                birlosVisualState = currentState.copy(
                    count = count,
                    birlosState = resizedBirlosState,
                    evaluated = resizedEvaluated,
                ),
            )
        }
    },
)
