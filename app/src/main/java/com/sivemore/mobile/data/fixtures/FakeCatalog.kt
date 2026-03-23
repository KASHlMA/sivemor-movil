package com.sivemore.mobile.data.fixtures

import com.sivemore.mobile.domain.model.AuthenticatedUser
import com.sivemore.mobile.domain.model.EvidenceItem
import com.sivemore.mobile.domain.model.EvidenceSource
import com.sivemore.mobile.domain.model.InspectionCategory
import com.sivemore.mobile.domain.model.InspectionCategoryContent
import com.sivemore.mobile.domain.model.InspectionItem
import com.sivemore.mobile.domain.model.InspectionItemInputMode
import com.sivemore.mobile.domain.model.InspectionOption
import com.sivemore.mobile.domain.model.InspectionSection
import com.sivemore.mobile.domain.model.VehicleStatus
import com.sivemore.mobile.domain.model.VehicleSummary
import com.sivemore.mobile.domain.model.VerificationSession
import com.sivemore.mobile.domain.model.VerificationSessionStatus

object FakeCatalog {

    const val approvedOptionId = "approved"

    val user = AuthenticatedUser(
        id = "usr-01",
        displayName = "Mariana Ortiz",
        email = "inspector@sivemor.mx",
    )

    fun defaultVehicles(): List<VehicleSummary> = listOf(
        VehicleSummary(
            id = "veh-003",
            plates = "VUH-TQ8-453",
            serialNumber = "8M3BMHB68B3286050",
            vehicleNumber = "003",
            status = VehicleStatus.Pending,
            admissionDate = "18-03-2026",
            completedDate = null,
            hasPendingVerification = true,
        ),
        VehicleSummary(
            id = "veh-002",
            plates = "MOR-TQ8-452",
            serialNumber = "4S3BMHB68B3286050",
            vehicleNumber = "002",
            status = VehicleStatus.Rejected,
            admissionDate = "15-02-2026",
            completedDate = "16-02-2026",
            hasPendingVerification = false,
        ),
        VehicleSummary(
            id = "veh-004",
            plates = "PUX-TL1-904",
            serialNumber = "1M8GDM9AXKP042788",
            vehicleNumber = "004",
            status = VehicleStatus.Approved,
            admissionDate = "08-03-2026",
            completedDate = "09-03-2026",
            hasPendingVerification = false,
        ),
    )

    fun defaultSessions(): List<VerificationSession> = listOf(
        createPendingSession("veh-003"),
        createFreshSession("veh-002"),
        createFreshSession("veh-004"),
    )

    fun createFreshSession(vehicleId: String): VerificationSession = VerificationSession(
        id = "session-$vehicleId",
        vehicleId = vehicleId,
        selectedCategory = InspectionCategory.Lights,
        status = VerificationSessionStatus.InProgress,
        categories = categoryContent(vehicleId),
        evidence = emptyList(),
        comments = "",
        updatedAtLabel = "Actualizado hoy",
    )

    fun createPendingSession(vehicleId: String): VerificationSession = createFreshSession(vehicleId).copy(
        selectedCategory = InspectionCategory.Tires,
        status = VerificationSessionStatus.InProgress,
        comments = "Pendiente validar detalle de la masa trasera y cerrar evidencia final.",
        evidence = listOf(
            EvidenceItem(
                id = "evidence-1",
                title = "Foto capturada",
                subtitle = "Vista frontal",
                source = EvidenceSource.Camera,
                addedAtLabel = "Hoy 18:55",
                accentColor = 0xFF9B4130,
            ),
            EvidenceItem(
                id = "evidence-2",
                title = "Evidencia seleccionada",
                subtitle = "Detalle del rin",
                source = EvidenceSource.Gallery,
                addedAtLabel = "Hoy 18:57",
                accentColor = 0xFF2E6A5D,
            ),
        ),
        categories = categoryContent(vehicleId).map { content ->
            if (content.category == InspectionCategory.Tires) {
                content.copy(
                    sections = content.sections.map { section ->
                        section.copy(
                            items = section.items.map { item ->
                                when (item.id) {
                                    "tires_front_rims" -> item.copy(selectedOptionIds = setOf(approvedOptionId))
                                    "tires_rear_rims" -> item.copy(selectedOptionIds = setOf("worn"))
                                    "tires_missing_lugs" -> item.copy(
                                        selectedOptionIds = setOf("missing"),
                                        numericValue = "88",
                                    )

                                    else -> item
                                }
                            },
                        )
                    },
                )
            } else {
                content
            }
        },
        updatedAtLabel = "Pendiente por concluir",
    )

    private fun categoryContent(vehicleId: String): List<InspectionCategoryContent> = listOf(
        InspectionCategoryContent(
            category = InspectionCategory.Lights,
            sections = listOf(
                section(
                    id = "lights-galibo-$vehicleId",
                    title = "Luces galibo",
                    item("lights_galibo", "Selecciona el resultado"),
                ),
                section(
                    id = "lights-high-$vehicleId",
                    title = "Luces altas",
                    item("lights_high", "Selecciona el resultado"),
                ),
                section(
                    id = "lights-low-$vehicleId",
                    title = "Luces bajas",
                    item("lights_low", "Selecciona el resultado"),
                ),
            ),
        ),
        InspectionCategoryContent(
            category = InspectionCategory.Tires,
            sections = listOf(
                section(
                    id = "tires-front-$vehicleId",
                    title = "Llantas rines delanteros",
                    item(
                        id = "tires_front_rims",
                        title = "Condición general",
                        options = defaultChecklist(),
                    ),
                ),
                section(
                    id = "tires-rear-$vehicleId",
                    title = "Llantas rines traseros",
                    item(
                        id = "tires_rear_rims",
                        title = "Condición general",
                        options = listOf(
                            approved(),
                            InspectionOption("worn", "Desgaste irregular"),
                            InspectionOption("loose", "Tuercas flojas"),
                            InspectionOption("broken", "Tuercas rotas"),
                        ),
                    ),
                ),
                section(
                    id = "tires-lugs-$vehicleId",
                    title = "Tuercas faltantes delantera derecha",
                    item(
                        id = "tires_missing_lugs",
                        title = "Resultado",
                        options = listOf(
                            approved(),
                            InspectionOption("missing", "Faltantes"),
                            InspectionOption("damaged", "Dañadas"),
                        ),
                        inputMode = InspectionItemInputMode.CheckboxesWithNumeric,
                        numericLabel = "PSI",
                        numericSuffix = "PSI",
                    ),
                ),
            ),
        ),
        InspectionCategoryContent(
            category = InspectionCategory.DirectionStructure,
            sections = listOf(
                section(
                    id = "direction-chavetas-$vehicleId",
                    title = "Chavetas",
                    item(
                        id = "direction_chavetas",
                        title = "Estado de chavetas",
                        options = listOf(
                            approved(),
                            InspectionOption("missing", "Faltan chavetas"),
                        ),
                    ),
                ),
                section(
                    id = "direction-access-$vehicleId",
                    title = "En caso de que hagan falta chavetas",
                    item(
                        id = "direction_missing_note",
                        title = "Describe el hallazgo",
                        options = listOf(
                            approved(),
                            InspectionOption("observed", "Agregar observación"),
                        ),
                        inputMode = InspectionItemInputMode.CheckboxesWithNote,
                        noteLabel = "Comentarios",
                    ),
                ),
                section(
                    id = "direction-doors-$vehicleId",
                    title = "Accesos y estructura",
                    item(
                        id = "direction_doors",
                        title = "Puertas y bisagras",
                        options = listOf(
                            approved(),
                            InspectionOption("misaligned", "Desalineadas"),
                            InspectionOption("damaged", "Golpeadas"),
                        ),
                    ),
                ),
            ),
        ),
        InspectionCategoryContent(
            category = InspectionCategory.AirBrakes,
            sections = listOf(
                section(
                    id = "air-hoses-$vehicleId",
                    title = "Sistema de aire / frenos",
                    item(
                        id = "air_hoses",
                        title = "Mangueras y conexiones",
                        options = listOf(
                            approved(),
                            InspectionOption("leak", "Con fuga"),
                            InspectionOption("cut", "Cortadas"),
                        ),
                    ),
                ),
                section(
                    id = "air-pressure-$vehicleId",
                    title = "Presión de aire",
                    item(
                        id = "air_pressure",
                        title = "Lectura",
                        options = listOf(
                            approved(),
                            InspectionOption("low", "Presión baja"),
                        ),
                        inputMode = InspectionItemInputMode.CheckboxesWithNumeric,
                        numericLabel = "PSI",
                        numericSuffix = "PSI",
                    ),
                ),
            ),
        ),
        InspectionCategoryContent(
            category = InspectionCategory.EngineEmissions,
            sections = listOf(
                section(
                    id = "engine-oil-$vehicleId",
                    title = "Motor y emisiones",
                    item(
                        id = "engine_oil",
                        title = "Fugas de aceite",
                        options = listOf(
                            approved(),
                            InspectionOption("present", "Se observan fugas"),
                        ),
                    ),
                ),
                section(
                    id = "engine-emissions-$vehicleId",
                    title = "Emisiones visibles",
                    item(
                        id = "engine_emissions",
                        title = "Describe el comportamiento",
                        options = listOf(
                            approved(),
                            InspectionOption("smoke", "Con humo"),
                            InspectionOption("odor", "Olor excesivo"),
                        ),
                        inputMode = InspectionItemInputMode.CheckboxesWithNote,
                        noteLabel = "Observación",
                    ),
                ),
            ),
        ),
        InspectionCategoryContent(
            category = InspectionCategory.Others,
            sections = listOf(
                section(
                    id = "others-extinguisher-$vehicleId",
                    title = "Extintor",
                    item(
                        id = "others_extinguisher",
                        title = "Disponibilidad y vigencia",
                        options = listOf(
                            approved(),
                            InspectionOption("expired", "Caducado"),
                            InspectionOption("missing", "No disponible"),
                        ),
                    ),
                ),
                section(
                    id = "others-cabin-$vehicleId",
                    title = "Cabina",
                    item(
                        id = "others_cabin",
                        title = "Comentarios generales",
                        options = listOf(
                            approved(),
                            InspectionOption("issue", "Agregar comentario"),
                        ),
                        inputMode = InspectionItemInputMode.CheckboxesWithNote,
                        noteLabel = "Comentario",
                    ),
                ),
            ),
        ),
        InspectionCategoryContent(
            category = InspectionCategory.Evidence,
            sections = listOf(
                section(
                    id = "evidence-grid-$vehicleId",
                    title = "Evidencias",
                    item(
                        id = "evidence_gallery",
                        title = "Registra frente, lateral y detalle del hallazgo",
                        inputMode = InspectionItemInputMode.EvidenceTiles,
                        helperText = "Usa el botón inferior para tomar foto o seleccionar evidencia.",
                    ),
                ),
            ),
        ),
    )

    private fun section(
        id: String,
        title: String,
        vararg items: InspectionItem,
    ) = InspectionSection(
        id = id,
        title = title,
        items = items.toList(),
    )

    private fun item(
        id: String,
        title: String,
        options: List<InspectionOption> = defaultChecklist(),
        inputMode: InspectionItemInputMode = InspectionItemInputMode.Checkboxes,
        noteLabel: String? = null,
        numericLabel: String? = null,
        numericSuffix: String? = null,
        helperText: String? = null,
    ) = InspectionItem(
        id = id,
        title = title,
        options = options,
        inputMode = inputMode,
        noteLabel = noteLabel,
        numericLabel = numericLabel,
        numericSuffix = numericSuffix,
        helperText = helperText,
    )

    private fun approved() = InspectionOption(approvedOptionId, "Aprobadas")

    private fun defaultChecklist(): List<InspectionOption> = listOf(
        approved(),
        InspectionOption("left_out", "Izquierda fundida"),
        InspectionOption("right_out", "Derecha fundida"),
        InspectionOption("both_out", "Ambas fundidas"),
    )
}
