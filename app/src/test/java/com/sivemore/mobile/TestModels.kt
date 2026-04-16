package com.sivemore.mobile

import com.sivemore.mobile.domain.model.AuthenticatedUser
import com.sivemore.mobile.domain.model.EvidenceItem
import com.sivemore.mobile.domain.model.InspectionItem
import com.sivemore.mobile.domain.model.InspectionOption
import com.sivemore.mobile.domain.model.InspectionSection
import com.sivemore.mobile.domain.model.VerificationSession
import com.sivemore.mobile.domain.model.VerificationSessionStatus
import com.sivemore.mobile.domain.model.VehicleStatus
import com.sivemore.mobile.domain.model.VehicleSummary

internal val sampleUser = AuthenticatedUser(
    id = 1L,
    username = "tecnico1",
    fullName = "Mariana Ortiz",
    role = "TECHNICIAN",
)

internal fun sampleVehicle(
    id: String = "1",
    hasPendingVerification: Boolean = false,
): VehicleSummary = VehicleSummary(
    id = id,
    editableVehicleId = "101",
    plates = "MOR-123-A",
    serialNumber = "ORD-2026-001",
    vehicleNumber = "Transportes Morelos",
    status = if (hasPendingVerification) VehicleStatus.InProgress else VehicleStatus.Assigned,
    admissionDate = "23/03/2026 10:00",
    completedDate = "Cuernavaca",
    hasPendingVerification = hasPendingVerification,
    draftInspectionId = if (hasPendingVerification) "90" else null,
)

internal fun sampleSession(orderUnitId: String = "1"): VerificationSession = VerificationSession(
    id = "90",
    orderUnitId = orderUnitId,
    orderNumber = "ORD-2026-001",
    vehiclePlate = "MOR-123-A",
    clientCompanyName = "Transportes Morelos",
    status = VerificationSessionStatus.InProgress,
    sections = listOf(
        InspectionSection(
            id = "10",
            title = "Luces",
            description = "Revision inicial",
            noteValue = "",
            items = listOf(
                InspectionItem(
                    id = "100",
                    title = "Luces frontales",
                    required = true,
                    options = listOf(
                        InspectionOption("PASS", "Cumple"),
                        InspectionOption("FAIL", "No cumple"),
                        InspectionOption("NA", "No aplica"),
                    ),
                    selectedOptionId = "PASS",
                    noteValue = "",
                ),
            ),
            evidence = listOf(
                EvidenceItem(
                    id = "e1",
                    title = "photo.jpg",
                    subtitle = "image/jpeg",
                    addedAtLabel = "23/03/2026 12:00",
                    accentColor = 0xFFD7EAD8,
                ),
                EvidenceItem(
                    id = "e2",
                    title = "photo-2.jpg",
                    subtitle = "image/jpeg",
                    addedAtLabel = "23/03/2026 12:05",
                    accentColor = 0xFFE9E0CB,
                ),
                EvidenceItem(
                    id = "e3",
                    title = "photo-3.jpg",
                    subtitle = "image/jpeg",
                    addedAtLabel = "23/03/2026 12:10",
                    accentColor = 0xFFD7EAD8,
                ),
            ),
        ),
        InspectionSection(
            id = "11",
            title = "Llantas",
            description = "Revision secundaria",
            noteValue = "",
            items = listOf(
                InspectionItem(
                    id = "101",
                    title = "Profundidad",
                    required = true,
                    options = listOf(
                        InspectionOption("PASS", "Cumple"),
                        InspectionOption("FAIL", "No cumple"),
                    ),
                    selectedOptionId = "PASS",
                    noteValue = "",
                ),
            ),
            evidence = emptyList(),
        ),
    ),
    comments = "",
    updatedAtLabel = "23/03/2026 12:00",
    evidenceCount = 3,
)

internal fun sampleIncompleteSession(orderUnitId: String = "1"): VerificationSession = sampleSession(orderUnitId).let { session ->
    session.copy(
        sections = session.sections.mapIndexed { index, section ->
            if (index == 1) {
                section.copy(items = section.items.map { it.copy(selectedOptionId = null) })
            } else {
                section
            }
        },
    )
}
