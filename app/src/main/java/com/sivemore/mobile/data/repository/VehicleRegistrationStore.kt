package com.sivemore.mobile.data.repository

import com.sivemore.mobile.domain.model.EvidenceItem
import com.sivemore.mobile.domain.model.InspectionSection
import com.sivemore.mobile.domain.model.Vehicle
import com.sivemore.mobile.domain.model.VehicleStatus
import com.sivemore.mobile.domain.model.VehicleSummary
import com.sivemore.mobile.domain.model.VerificationSession
import com.sivemore.mobile.domain.model.VerificationSessionStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class VehicleRegistrationStore @Inject constructor() {
    private val mutex = Mutex()
    private val vehicles = linkedMapOf<String, Vehicle>()
    private val sessions = linkedMapOf<String, VerificationSession>()
    private val locale = Locale.forLanguageTag("es-MX")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", locale)

    suspend fun saveVehicle(vehicle: Vehicle): Vehicle = mutex.withLock {
        vehicles[vehicle.id] = vehicle
        sessions.putIfAbsent(vehicle.id, vehicle.toVerificationSession(timestamp = nowLabel()))
        vehicle
    }

    suspend fun loadVehicles(): List<VehicleSummary> = mutex.withLock {
        vehicles.values.map { it.toSummary(admissionDate = todayLabel()) }
    }

    suspend fun loadVehicle(vehicleId: String): VehicleSummary? = mutex.withLock {
        vehicles[vehicleId]?.toSummary(admissionDate = todayLabel())
    }

    suspend fun loadEditableVehicle(vehicleId: String): Vehicle? = mutex.withLock {
        vehicles[vehicleId]
    }

    suspend fun loadSession(vehicleId: String): VerificationSession? = mutex.withLock {
        sessions[vehicleId]
    }

    suspend fun updateComments(vehicleId: String, value: String): VerificationSession? = mutex.withLock {
        sessions[vehicleId]?.let { session ->
            val updated = session.copy(
                comments = value,
                updatedAtLabel = nowLabel(),
            )
            sessions[vehicleId] = updated
            updated
        }
    }

    suspend fun addEvidence(
        vehicleId: String,
        sectionId: String,
        evidenceId: String,
        fileName: String,
        previewUri: String?,
    ): VerificationSession? = mutex.withLock {
        sessions[vehicleId]?.let { session ->
            val updatedSections = session.sections.map { section ->
                if (section.id != sectionId) {
                    section
                } else {
                    section.copy(
                        evidence = section.evidence + EvidenceItem(
                            id = evidenceId,
                            title = fileName,
                            subtitle = "Evidencia capturada",
                            addedAtLabel = nowLabel(),
                            accentColor = 0xFF5D8574,
                            previewUri = previewUri,
                        ),
                    )
                }
            }
            val updated = session.copy(
                sections = updatedSections,
                evidenceCount = updatedSections.sumOf { it.evidence.size },
                updatedAtLabel = nowLabel(),
            )
            sessions[vehicleId] = updated
            updated
        }
    }

    suspend fun removeEvidence(vehicleId: String, evidenceId: String): VerificationSession? = mutex.withLock {
        sessions[vehicleId]?.let { session ->
            val updatedSections = session.sections.map { section ->
                section.copy(evidence = section.evidence.filterNot { it.id == evidenceId })
            }
            val updated = session.copy(
                sections = updatedSections,
                evidenceCount = updatedSections.sumOf { it.evidence.size },
                updatedAtLabel = nowLabel(),
            )
            sessions[vehicleId] = updated
            updated
        }
    }

    suspend fun completeSession(vehicleId: String) = mutex.withLock {
        sessions[vehicleId]?.let { session ->
            sessions[vehicleId] = session.copy(
                status = VerificationSessionStatus.Completed,
                updatedAtLabel = nowLabel(),
            )
        }
    }

    suspend fun pauseSession(vehicleId: String) = mutex.withLock {
        sessions[vehicleId]?.let { session ->
            sessions[vehicleId] = session.copy(
                status = VerificationSessionStatus.Paused,
                updatedAtLabel = nowLabel(),
            )
        }
    }

    suspend fun abandonSession(vehicleId: String) = mutex.withLock {
        sessions.remove(vehicleId)
    }

    private fun todayLabel(): String = LocalDateTime.now().format(dateFormatter)

    private fun nowLabel(): String = LocalDateTime.now().format(dateTimeFormatter)
}

private fun Vehicle.toSummary(admissionDate: String): VehicleSummary = VehicleSummary(
    id = id,
    editableVehicleId = id,
    plates = placas,
    serialNumber = vin,
    vehicleNumber = numeroEconomico,
    status = VehicleStatus.Assigned,
    admissionDate = admissionDate,
    completedDate = null,
    hasPendingVerification = false,
    draftInspectionId = null,
)

private fun Vehicle.toVerificationSession(timestamp: String): VerificationSession = VerificationSession(
    id = "session_$id",
    orderUnitId = id,
    orderNumber = numeroEconomico,
    vehiclePlate = placas,
    clientCompanyName = "$marca $modelo",
    status = VerificationSessionStatus.InProgress,
    sections = listOf(
        InspectionSection(id = "1", title = "Luces", description = "Revision inicial de luces.", noteValue = "", items = emptyList(), evidence = emptyList()),
        InspectionSection(id = "2", title = "Llantas", description = "Revision inicial de llantas.", noteValue = "", items = emptyList(), evidence = emptyList()),
        InspectionSection(id = "3", title = "Direccion", description = "Revision inicial de direccion.", noteValue = "", items = emptyList(), evidence = emptyList()),
        InspectionSection(id = "4", title = "Aire y frenos", description = "Revision inicial de aire y frenos.", noteValue = "", items = emptyList(), evidence = emptyList()),
        InspectionSection(id = "5", title = "Motor", description = "Revision inicial de motor.", noteValue = "", items = emptyList(), evidence = emptyList()),
        InspectionSection(id = "6", title = "Otros", description = "Revision general.", noteValue = "", items = emptyList(), evidence = emptyList()),
    ),
    comments = "",
    updatedAtLabel = timestamp,
    evidenceCount = 0,
)
