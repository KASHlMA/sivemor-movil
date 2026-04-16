package com.sivemore.mobile.data.network

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InspectionDraftResolver @Inject constructor(
    private val mobileApiService: MobileApiService,
) {
    private val draftIds = mutableMapOf<String, Long>()

    suspend fun resolveInspectionId(orderUnitId: String): Long {
        draftIds[orderUnitId]?.let { return it }

        val order = mobileApiService.listOrders()
            .firstOrNull { it.orderUnitId.toString() == orderUnitId }

        val draftId = when {
            order?.draftInspectionId != null -> order.draftInspectionId
            order != null -> mobileApiService
                .createInspection(CreateInspectionRequestDto(orderUnitId = orderUnitId.toLong()))
                .id

            else -> mobileApiService
                .createInspection(CreateInspectionRequestDto(vehicleUnitId = orderUnitId.toLong()))
                .id
        }

        draftIds[orderUnitId] = draftId
        return draftId
    }

    fun remember(orderUnitId: String, inspectionId: Long) {
        draftIds[orderUnitId] = inspectionId
    }

    fun forget(orderUnitId: String) {
        draftIds.remove(orderUnitId)
    }
}
