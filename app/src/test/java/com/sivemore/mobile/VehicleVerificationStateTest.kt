package com.sivemore.mobile

import com.sivemore.mobile.data.network.AssignedOrderDto
import com.sivemore.mobile.data.network.ChecklistQuestionDto
import com.sivemore.mobile.data.network.CreateInspectionRequestDto
import com.sivemore.mobile.data.network.CreateVehicleRequestDto
import com.sivemore.mobile.data.network.InspectionDraftDto
import com.sivemore.mobile.data.network.InspectionQuestionAnswerDto
import com.sivemore.mobile.data.network.InspectionSectionDraftDto
import com.sivemore.mobile.data.network.MobileApiService
import com.sivemore.mobile.data.network.QuestionUpdateDto
import com.sivemore.mobile.data.network.SectionUpdateDto
import com.sivemore.mobile.data.network.UpdateInspectionRequestDto
import com.sivemore.mobile.data.network.VehicleClientDto
import com.sivemore.mobile.data.network.VehicleDto
import com.sivemore.mobile.data.network.VehicleRegionDto
import com.sivemore.mobile.data.repository.RealVehicleRepository
import com.sivemore.mobile.data.repository.VehicleRegistrationStore
import com.sivemore.mobile.domain.model.Vehicle
import com.sivemore.mobile.domain.model.VehicleStatus
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleVerificationStateTest {
    @Test
    fun localPausedSessionAppearsAsPending() = runTest {
        val store = VehicleRegistrationStore()
        val vehicle = Vehicle(
            id = "local-1",
            numeroEconomico = "1500",
            placas = "MOR-123-A",
            marca = "Nissan",
            modelo = "NP300",
            tipoVehiculo = "N2",
            vin = "VIN-123",
        )

        store.saveVehicle(vehicle)
        store.pauseSession(vehicle.id)

        val summary = store.loadVehicle(vehicle.id)

        assertEquals(VehicleStatus.Paused, summary?.status)
        assertTrue(summary?.hasPendingVerification == true)
    }

    @Test
    fun remoteInProgressDraftDoesNotAppearAsPausedPending() = runTest {
        val repository = RealVehicleRepository(
            mobileApiService = FakeMobileApiService(draftStatus = "IN_PROGRESS"),
            registrationStore = VehicleRegistrationStore(),
        )

        val vehicle = repository.loadVehicle("1")

        assertEquals(VehicleStatus.Assigned, vehicle?.status)
        assertFalse(vehicle?.hasPendingVerification == true)
    }

    @Test
    fun remotePausedDraftAppearsAsPending() = runTest {
        val repository = RealVehicleRepository(
            mobileApiService = FakeMobileApiService(draftStatus = "PAUSED"),
            registrationStore = VehicleRegistrationStore(),
        )

        val vehicle = repository.loadVehicle("1")

        assertEquals(VehicleStatus.Paused, vehicle?.status)
        assertTrue(vehicle?.hasPendingVerification == true)
    }
}

private class FakeMobileApiService(
    private val draftStatus: String,
) : MobileApiService {
    override suspend fun listOrders(): List<AssignedOrderDto> = listOf(
        AssignedOrderDto(
            orderUnitId = 1,
            orderId = 10,
            orderNumber = "ORD-1",
            clientCompanyId = 1500,
            clientCompanyName = "Transportes Morelos",
            regionName = "Cuernavaca",
            scheduledAt = "2026-04-17T10:00:00Z",
            vehicleUnitId = 100,
            vehiclePlate = "MOR-123-A",
            vehicleCategory = "N2",
            draftInspectionId = 90,
        ),
    )

    override suspend fun getInspection(inspectionId: Long): InspectionDraftDto = InspectionDraftDto(
        id = inspectionId,
        orderId = 10,
        orderUnitId = 1,
        orderNumber = "ORD-1",
        vehiclePlate = "MOR-123-A",
        clientCompanyName = "Transportes Morelos",
        status = draftStatus,
        lastSectionIndex = 0,
        overallComment = null,
        startedAt = "2026-04-17T10:00:00Z",
        updatedAt = "2026-04-17T10:00:00Z",
        evidenceCount = 0,
        sections = listOf(
            InspectionSectionDraftDto(
                sectionId = 1,
                title = "Luces",
                description = null,
                displayOrder = 0,
                note = null,
                questions = listOf(
                    ChecklistQuestionDto(
                        id = 100,
                        code = "LUCES_1",
                        prompt = "Pregunta",
                        required = true,
                        displayOrder = 0,
                    ),
                ),
                answers = listOf(
                    InspectionQuestionAnswerDto(
                        questionId = 100,
                        answer = "NA",
                        comment = null,
                    ),
                ),
                evidences = emptyList(),
            ),
        ),
    )

    override suspend fun listClients(): List<VehicleClientDto> = emptyList()

    override suspend fun listRegions(): List<VehicleRegionDto> = emptyList()

    override suspend fun createVehicle(request: CreateVehicleRequestDto): VehicleDto = error("unused")

    override suspend fun getVehicle(vehicleId: Long): VehicleDto = error("unused")

    override suspend fun updateVehicle(vehicleId: Long, request: CreateVehicleRequestDto): VehicleDto = error("unused")

    override suspend fun createInspection(request: CreateInspectionRequestDto): InspectionDraftDto = error("unused")

    override suspend fun updateInspection(
        inspectionId: Long,
        request: UpdateInspectionRequestDto,
    ): InspectionDraftDto = error("unused")

    override suspend fun addEvidence(
        inspectionId: Long,
        file: MultipartBody.Part,
        sectionId: Long,
        comment: String?,
    ): InspectionDraftDto = error("unused")

    override suspend fun deleteEvidence(
        inspectionId: Long,
        evidenceId: Long,
    ): InspectionDraftDto = error("unused")

    override suspend fun pauseInspection(inspectionId: Long): InspectionDraftDto = error("unused")

    override suspend fun abandonInspection(inspectionId: Long) = Unit

    override suspend fun submitInspection(inspectionId: Long) = Unit
}
