package com.sivemore.mobile.data.repository

import com.sivemore.mobile.data.network.InspectionDraftDto
import com.sivemore.mobile.data.network.InspectionDraftResolver
import com.sivemore.mobile.data.network.MediaUploadResolver
import com.sivemore.mobile.data.network.MobileApiService
import com.sivemore.mobile.data.network.QuestionUpdateDto
import com.sivemore.mobile.data.network.SectionUpdateDto
import com.sivemore.mobile.data.network.UpdateInspectionRequestDto
import com.sivemore.mobile.data.network.toDomain
import com.sivemore.mobile.domain.model.EvidenceUpload
import com.sivemore.mobile.domain.model.VerificationSession
import com.sivemore.mobile.domain.repository.VerificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealVerificationRepository @Inject constructor(
    private val mobileApiService: MobileApiService,
    private val draftResolver: InspectionDraftResolver,
    private val mediaUploadResolver: MediaUploadResolver,
) : VerificationRepository {
    override suspend fun loadSession(orderUnitId: String): VerificationSession {
        val inspectionId = draftResolver.resolveInspectionId(orderUnitId)
        val draft = mobileApiService.getInspection(inspectionId)
        draftResolver.remember(orderUnitId, draft.id)
        return draft.toDomain()
    }

    override suspend fun updateQuestionAnswer(
        orderUnitId: String,
        sectionId: String,
        questionId: String,
        optionId: String,
    ): VerificationSession = updateDraft(orderUnitId, sectionId.toLong()) { draft ->
        buildFullUpdate(draft) { section ->
            if (section.sectionId.toString() == sectionId) {
                section.toSectionUpdate(
                    answerOverrides = mapOf(
                        questionId.toLong() to QuestionUpdateDto(
                            questionId = questionId.toLong(),
                            answer = optionId,
                            comment = section.answers.firstOrNull { it.questionId.toString() == questionId }?.comment,
                        )
                    )
                )
            } else {
                section.toSectionUpdate()
            }
        }
    }

    override suspend fun updateQuestionComment(
        orderUnitId: String,
        sectionId: String,
        questionId: String,
        value: String,
    ): VerificationSession = updateDraft(orderUnitId, sectionId.toLong()) { draft ->
        buildFullUpdate(draft) { section ->
            if (section.sectionId.toString() == sectionId) {
                val existingAnswer = section.answers.firstOrNull { it.questionId.toString() == questionId }
                section.toSectionUpdate(
                    answerOverrides = mapOf(
                        questionId.toLong() to QuestionUpdateDto(
                            questionId = questionId.toLong(),
                            answer = existingAnswer?.answer ?: "NA",
                            comment = value.ifBlank { null },
                        )
                    )
                )
            } else {
                section.toSectionUpdate()
            }
        }
    }

    override suspend fun updateSectionNote(
        orderUnitId: String,
        sectionId: String,
        value: String,
    ): VerificationSession = updateDraft(orderUnitId, sectionId.toLong()) { draft ->
        buildFullUpdate(draft) { section ->
            if (section.sectionId.toString() == sectionId) {
                section.toSectionUpdate(noteOverride = value.ifBlank { null })
            } else {
                section.toSectionUpdate()
            }
        }
    }

    override suspend fun updateComments(
        orderUnitId: String,
        value: String,
    ): VerificationSession {
        val inspectionId = draftResolver.resolveInspectionId(orderUnitId)
        return mobileApiService.updateInspection(
            inspectionId = inspectionId,
            request = UpdateInspectionRequestDto(overallComment = value.ifBlank { null }),
        ).toDomain()
    }

    override suspend fun addEvidence(
        orderUnitId: String,
        sectionId: String,
        upload: EvidenceUpload,
    ): VerificationSession {
        val inspectionId = draftResolver.resolveInspectionId(orderUnitId)
        return mobileApiService.addEvidence(
            inspectionId = inspectionId,
            file = mediaUploadResolver.toMultipart(upload),
            sectionId = mediaUploadResolver.textPart(sectionId),
            comment = null,
        ).toDomain()
    }

    override suspend fun removeEvidence(
        orderUnitId: String,
        evidenceId: String,
    ): VerificationSession {
        val inspectionId = draftResolver.resolveInspectionId(orderUnitId)
        return mobileApiService.deleteEvidence(inspectionId, evidenceId.toLong()).toDomain()
    }

    override suspend fun pauseSession(orderUnitId: String) {
        val inspectionId = draftResolver.resolveInspectionId(orderUnitId)
        mobileApiService.pauseInspection(inspectionId)
    }

    override suspend fun completeSession(orderUnitId: String) {
        val inspectionId = draftResolver.resolveInspectionId(orderUnitId)
        mobileApiService.submitInspection(inspectionId)
    }

    override suspend fun abandonSession(orderUnitId: String) {
        val inspectionId = draftResolver.resolveInspectionId(orderUnitId)
        mobileApiService.abandonInspection(inspectionId)
        draftResolver.forget(orderUnitId)
    }

    private suspend fun updateDraft(
        orderUnitId: String,
        sectionId: Long,
        requestBuilder: (InspectionDraftDto) -> UpdateInspectionRequestDto,
    ): VerificationSession {
        val inspectionId = draftResolver.resolveInspectionId(orderUnitId)
        val draft = mobileApiService.getInspection(inspectionId)
        val updated = mobileApiService.updateInspection(
            inspectionId = inspectionId,
            request = requestBuilder(draft).copy(
                lastSectionIndex = draft.sections
                    .sortedBy { it.displayOrder }
                    .indexOfFirst { it.sectionId == sectionId }
                    .coerceAtLeast(0)
            ),
        )
        return updated.toDomain()
    }

    private fun buildFullUpdate(
        draft: InspectionDraftDto,
        sectionBuilder: (com.sivemore.mobile.data.network.InspectionSectionDraftDto) -> SectionUpdateDto,
    ): UpdateInspectionRequestDto = UpdateInspectionRequestDto(
        overallComment = draft.overallComment,
        sections = draft.sections.sortedBy { it.displayOrder }.map(sectionBuilder),
    )

    private fun com.sivemore.mobile.data.network.InspectionSectionDraftDto.toSectionUpdate(
        noteOverride: String? = note,
        answerOverrides: Map<Long, QuestionUpdateDto> = emptyMap(),
    ): SectionUpdateDto {
        val existingAnswers = answers.associateBy { it.questionId }
        return SectionUpdateDto(
            sectionId = sectionId,
            note = noteOverride,
            questions = questions.sortedBy { it.displayOrder }.map { question ->
                answerOverrides[question.id] ?: existingAnswers[question.id]?.let {
                    QuestionUpdateDto(
                        questionId = it.questionId,
                        answer = it.answer,
                        comment = it.comment,
                    )
                } ?: QuestionUpdateDto(
                    questionId = question.id,
                    answer = "NA",
                    comment = null,
                )
            },
        )
    }
}
