package com.sivemore.mobile.domain.model

enum class VehicleStatus(val label: String) {
    Pending("Pendiente"),
    Approved("Aprobado"),
    Rejected("Reprobado"),
}

enum class VerificationSessionStatus {
    InProgress,
    Paused,
    Completed,
}

enum class InspectionCategory(val label: String) {
    Lights("Luces"),
    Tires("Llantas"),
    DirectionStructure("Dirección"),
    AirBrakes("Aire/Frenos"),
    EngineEmissions("Motor"),
    Others("Otros"),
    Evidence("Evidencias"),
}

enum class InspectionItemInputMode {
    Checkboxes,
    CheckboxesWithNote,
    CheckboxesWithNumeric,
    EvidenceTiles,
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
)

data class VerificationSession(
    val id: String,
    val vehicleId: String,
    val selectedCategory: InspectionCategory,
    val status: VerificationSessionStatus,
    val categories: List<InspectionCategoryContent>,
    val evidence: List<EvidenceItem>,
    val comments: String,
    val updatedAtLabel: String,
)

data class InspectionCategoryContent(
    val category: InspectionCategory,
    val sections: List<InspectionSection>,
)

data class InspectionSection(
    val id: String,
    val title: String,
    val items: List<InspectionItem>,
)

data class InspectionItem(
    val id: String,
    val title: String,
    val options: List<InspectionOption> = emptyList(),
    val inputMode: InspectionItemInputMode = InspectionItemInputMode.Checkboxes,
    val selectedOptionIds: Set<String> = emptySet(),
    val noteLabel: String? = null,
    val noteValue: String = "",
    val numericLabel: String? = null,
    val numericValue: String = "",
    val numericSuffix: String? = null,
    val helperText: String? = null,
)

data class InspectionOption(
    val id: String,
    val label: String,
)

data class EvidenceItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val source: EvidenceSource,
    val addedAtLabel: String,
    val accentColor: Long,
)
