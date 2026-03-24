package com.sivemore.mobile.data.network

import com.sivemore.mobile.domain.model.AuthenticatedUser
import com.sivemore.mobile.domain.model.EvidenceItem
import com.sivemore.mobile.domain.model.InspectionItem
import com.sivemore.mobile.domain.model.InspectionOption
import com.sivemore.mobile.domain.model.InspectionSection
import com.sivemore.mobile.domain.model.VerificationSession
import com.sivemore.mobile.domain.model.VerificationSessionStatus
import com.sivemore.mobile.domain.model.VehicleStatus
import com.sivemore.mobile.domain.model.VehicleSummary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun SessionUserDto.toDomain(): AuthenticatedUser = AuthenticatedUser(
    id = id,
    username = username,
    fullName = fullName,
    role = role,
)

fun AssignedOrderDto.toDomain(): VehicleSummary = VehicleSummary(
    id = orderUnitId.toString(),
    plates = vehiclePlate,
    serialNumber = orderNumber,
    vehicleNumber = clientCompanyName,
    status = when {
        draftInspectionId == null -> VehicleStatus.Assigned
        else -> VehicleStatus.InProgress
    },
    admissionDate = scheduledAt.toDisplayDateTime(),
    completedDate = regionName,
    hasPendingVerification = draftInspectionId != null,
    draftInspectionId = draftInspectionId?.toString(),
)

fun InspectionDraftDto.toDomain(): VerificationSession = VerificationSession(
    id = id.toString(),
    orderUnitId = orderUnitId.toString(),
    orderNumber = orderNumber,
    vehiclePlate = vehiclePlate,
    clientCompanyName = clientCompanyName,
    status = when (status) {
        "PAUSED" -> VerificationSessionStatus.Paused
        "SUBMITTED" -> VerificationSessionStatus.Completed
        else -> VerificationSessionStatus.InProgress
    },
    sections = sections.sortedBy { it.displayOrder }.map { section ->
        val answersByQuestionId = section.answers.associateBy { it.questionId }
        InspectionSection(
            id = section.sectionId.toString(),
            title = section.title,
            description = section.description,
            noteValue = section.note.orEmpty(),
            items = section.questions.sortedBy { it.displayOrder }.map { question ->
                val answer = answersByQuestionId[question.id]
                InspectionItem(
                    id = question.id.toString(),
                    title = question.prompt,
                    required = question.required,
                    options = ANSWER_OPTIONS,
                    selectedOptionId = answer?.answer,
                    noteValue = answer?.comment.orEmpty(),
                )
            },
            evidence = section.evidences.mapIndexed { index, evidence ->
                EvidenceItem(
                    id = evidence.id.toString(),
                    title = evidence.filename,
                    subtitle = evidence.comment ?: evidence.mimeType,
                    addedAtLabel = evidence.capturedAt.toDisplayDateTime(),
                    accentColor = if (index % 2 == 0) 0xFFD7EAD8 else 0xFFE9E0CB,
                )
            },
        )
    },
    comments = overallComment.orEmpty(),
    updatedAtLabel = updatedAt.toDisplayDateTime(),
    evidenceCount = evidenceCount,
)

private fun String.toDisplayDateTime(): String = runCatching {
    Instant.parse(this)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
}.getOrDefault(this)

private val ANSWER_OPTIONS = listOf(
    InspectionOption(id = "PASS", label = "Cumple"),
    InspectionOption(id = "FAIL", label = "No cumple"),
    InspectionOption(id = "NA", label = "No aplica"),
)
