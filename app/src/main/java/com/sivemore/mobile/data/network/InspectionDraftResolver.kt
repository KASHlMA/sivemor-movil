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
            ?: error("No se encontró una orden asignada para la unidad $orderUnitId.")

        val draftId = order.draftInspectionId ?: mobileApiService
            .createInspection(CreateInspectionRequestDto(orderUnitId = orderUnitId.toLong()))
            .id
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
