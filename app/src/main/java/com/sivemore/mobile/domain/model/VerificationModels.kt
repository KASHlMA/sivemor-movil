package com.sivemore.mobile.domain.model

enum class VehicleStatus(val label: String) {
    Assigned("Aceptado"),
    InProgress("Reprobado"),
    Paused("Reprobado"),
}

enum class VerificationSessionStatus {
    InProgress,
    Paused,
    Completed,
}

data class InspectionCategory(
    val id: String,
    val label: String,
) {
    val name: String
        get() = id
}

enum class EvidenceSource(val label: String) {
    Camera("Tomar foto"),
    Gallery("Seleccionar evidencia"),
}

data class VehicleSummary(
    val id: String,
    val plates: String,
    val serialNumber: String,
    val vehicleNumber: String,
    val status: VehicleStatus,
    val admissionDate: String,
    val completedDate: String?,
    val hasPendingVerification: Boolean,
    val draftInspectionId: String?,
)

data class VerificationSession(
    val id: String,
    val orderUnitId: String,
    val orderNumber: String,
    val vehiclePlate: String,
    val clientCompanyName: String,
    val status: VerificationSessionStatus,
    val sections: List<InspectionSection>,
    val comments: String,
    val updatedAtLabel: String,
    val evidenceCount: Int,
)

data class InspectionFlowAnswerDraft(
    val questionCode: String,
    val answer: String,
    val comment: String = "",
)

data class InspectionSection(
    val id: String,
    val title: String,
    val description: String?,
    val noteValue: String,
    val items: List<InspectionItem>,
    val evidence: List<EvidenceItem>,
)

data class InspectionItem(
    val id: String,
    val title: String,
    val required: Boolean,
    val options: List<InspectionOption>,
    val selectedOptionId: String?,
    val noteValue: String,
)

data class InspectionOption(
    val id: String,
    val label: String,
)

data class EvidenceItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val addedAtLabel: String,
    val accentColor: Long,
    val previewUri: String? = null,
)

data class EvidenceUpload(
    val uri: String,
    val fileName: String?,
    val mimeType: String?,
)
