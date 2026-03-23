package com.sivemore.mobile.data.repository

import com.sivemore.mobile.data.fixtures.FakeCatalog
import com.sivemore.mobile.domain.model.EvidenceItem
import com.sivemore.mobile.domain.model.EvidenceSource
import com.sivemore.mobile.domain.model.InspectionCategory
import com.sivemore.mobile.domain.model.InspectionItem
import com.sivemore.mobile.domain.model.InspectionOption
import com.sivemore.mobile.domain.model.VehicleStatus
import com.sivemore.mobile.domain.model.VehicleSummary
import com.sivemore.mobile.domain.model.VerificationSession
import com.sivemore.mobile.domain.model.VerificationSessionStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeVerificationStore @Inject constructor() {

    private val vehicles = FakeCatalog.defaultVehicles().associateBy { it.id }.toMutableMap()
    private val sessions = FakeCatalog.defaultSessions().associateBy { it.vehicleId }.toMutableMap()
    private var evidenceCounter = 10

    fun loadVehicles(query: String): List<VehicleSummary> {
        val normalizedQuery = query.trim()
        return vehicles.values
            .sortedByDescending { it.admissionDate }
            .filter { vehicle ->
                normalizedQuery.isBlank() ||
                    vehicle.plates.contains(normalizedQuery, ignoreCase = true) ||
                    vehicle.serialNumber.contains(normalizedQuery, ignoreCase = true) ||
                    vehicle.vehicleNumber.contains(normalizedQuery, ignoreCase = true)
            }
    }

    fun loadVehicle(vehicleId: String): VehicleSummary? = vehicles[vehicleId]

    fun loadSession(vehicleId: String): VerificationSession {
        val existing = sessions[vehicleId]
        if (existing != null) {
            return existing
        }
        val created = FakeCatalog.createFreshSession(vehicleId)
        sessions[vehicleId] = created
        syncVehicle(
            vehicleId = vehicleId,
            status = VehicleStatus.Pending,
            completedDate = null,
            hasPendingVerification = true,
        )
        return created
    }

    fun setActiveCategory(
        vehicleId: String,
        category: InspectionCategory,
    ): VerificationSession = updateSession(vehicleId) {
        it.copy(
            selectedCategory = category,
            updatedAtLabel = "Actualizado hace un momento",
        )
    }

    fun toggleOption(
        vehicleId: String,
        itemId: String,
        optionId: String,
    ): VerificationSession = updateItem(vehicleId, itemId) { item ->
        val updatedSelection = if (optionId in item.selectedOptionIds) {
            emptySet()
        } else {
            setOf(optionId)
        }
        item.copy(selectedOptionIds = updatedSelection)
    }

    fun updateNote(
        vehicleId: String,
        itemId: String,
        value: String,
    ): VerificationSession = updateItem(vehicleId, itemId) { item ->
        item.copy(noteValue = value)
    }

    fun updateNumeric(
        vehicleId: String,
        itemId: String,
        value: String,
    ): VerificationSession = updateItem(vehicleId, itemId) { item ->
        item.copy(numericValue = value)
    }

    fun updateComments(
        vehicleId: String,
        value: String,
    ): VerificationSession = updateSession(vehicleId) {
        it.copy(
            comments = value,
            updatedAtLabel = "Comentario guardado",
        )
    }

    fun addEvidence(
        vehicleId: String,
        source: EvidenceSource,
    ): VerificationSession = updateSession(vehicleId) { session ->
        evidenceCounter += 1
        val evidence = EvidenceItem(
            id = "evidence-$evidenceCounter",
            title = if (source == EvidenceSource.Camera) {
                "Foto capturada"
            } else {
                "Evidencia seleccionada"
            },
            subtitle = if (source == EvidenceSource.Camera) {
                "Unidad frontal"
            } else {
                "Detalle del hallazgo"
            },
            source = source,
            addedAtLabel = "Hoy 19:20",
            accentColor = if (source == EvidenceSource.Camera) {
                0xFFB84C3A
            } else {
                0xFF316D61
            },
        )
        session.copy(
            evidence = session.evidence + evidence,
            selectedCategory = InspectionCategory.Evidence,
            updatedAtLabel = "Evidencia agregada",
        )
    }

    fun removeEvidence(
        vehicleId: String,
        evidenceId: String,
    ): VerificationSession = updateSession(vehicleId) { session ->
        session.copy(
            evidence = session.evidence.filterNot { it.id == evidenceId },
            updatedAtLabel = "Evidencia eliminada",
        )
    }

    fun pauseSession(vehicleId: String) {
        updateSession(vehicleId) {
            it.copy(
                status = VerificationSessionStatus.Paused,
                updatedAtLabel = "Verificación pausada",
            )
        }
        syncVehicle(
            vehicleId = vehicleId,
            status = VehicleStatus.Pending,
            completedDate = null,
            hasPendingVerification = true,
        )
    }

    fun completeSession(vehicleId: String) {
        val session = updateSession(vehicleId) {
            it.copy(
                status = VerificationSessionStatus.Completed,
                updatedAtLabel = "Verificación finalizada",
            )
        }
        syncVehicle(
            vehicleId = vehicleId,
            status = deriveVehicleStatus(session),
            completedDate = "18-03-2026",
            hasPendingVerification = false,
        )
    }

    fun abandonSession(vehicleId: String) {
        val session = sessions[vehicleId] ?: return
        syncVehicle(
            vehicleId = vehicleId,
            status = if (session.status == VerificationSessionStatus.Completed) {
                deriveVehicleStatus(session)
            } else {
                VehicleStatus.Pending
            },
            completedDate = if (session.status == VerificationSessionStatus.Completed) {
                "18-03-2026"
            } else {
                null
            },
            hasPendingVerification = session.status != VerificationSessionStatus.Completed,
        )
    }

    private fun updateItem(
        vehicleId: String,
        itemId: String,
        transform: (InspectionItem) -> InspectionItem,
    ): VerificationSession = updateSession(vehicleId) { session ->
        session.copy(
            categories = session.categories.map { category ->
                category.copy(
                    sections = category.sections.map { section ->
                        section.copy(
                            items = section.items.map { item ->
                                if (item.id == itemId) {
                                    transform(item)
                                } else {
                                    item
                                }
                            },
                        )
                    },
                )
            },
            updatedAtLabel = "Actualizado hace un momento",
        )
    }

    private fun updateSession(
        vehicleId: String,
        transform: (VerificationSession) -> VerificationSession,
    ): VerificationSession {
        val current = loadSession(vehicleId)
        val updated = transform(current)
        sessions[vehicleId] = updated
        return updated
    }

    private fun syncVehicle(
        vehicleId: String,
        status: VehicleStatus,
        completedDate: String?,
        hasPendingVerification: Boolean,
    ) {
        val current = vehicles[vehicleId] ?: return
        vehicles[vehicleId] = current.copy(
            status = status,
            completedDate = completedDate,
            hasPendingVerification = hasPendingVerification,
        )
    }

    private fun deriveVehicleStatus(session: VerificationSession): VehicleStatus {
        val selectedOptions = session.categories
            .flatMap { it.sections }
            .flatMap { it.items }
            .flatMap { item ->
                item.options.filter { option -> option.id in item.selectedOptionIds }
            }
        return if (selectedOptions.any { it.isFailure() }) {
            VehicleStatus.Rejected
        } else {
            VehicleStatus.Approved
        }
    }

    private fun InspectionOption.isFailure(): Boolean = id != FakeCatalog.approvedOptionId
}
