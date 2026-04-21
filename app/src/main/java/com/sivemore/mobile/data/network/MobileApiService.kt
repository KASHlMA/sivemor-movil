package com.sivemore.mobile.data.network

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface MobileApiService {
    @GET("mobile/orders")
    suspend fun listOrders(): List<AssignedOrderDto>

    @GET("mobile/clients")
    suspend fun listClients(): List<VehicleClientDto>

    @GET("mobile/regions")
    suspend fun listRegions(): List<VehicleRegionDto>

    @POST("mobile/vehicles")
    suspend fun createVehicle(@Body request: CreateVehicleRequestDto): VehicleDto

    @GET("mobile/vehicles/{id}")
    suspend fun getVehicle(@Path("id") vehicleId: Long): VehicleDto

    @PUT("mobile/vehicles/{id}")
    suspend fun updateVehicle(
        @Path("id") vehicleId: Long,
        @Body request: UpdateVehicleRequestDto,
    ): VehicleDto

    @POST("mobile/inspections")
    suspend fun createInspection(@Body request: CreateInspectionRequestDto): InspectionDraftDto

    @GET("mobile/inspections/{id}")
    suspend fun getInspection(@Path("id") inspectionId: Long): InspectionDraftDto

    @PUT("mobile/inspections/{id}")
    suspend fun updateInspection(
        @Path("id") inspectionId: Long,
        @Body request: UpdateInspectionRequestDto,
    ): InspectionDraftDto

    @Multipart
    @POST("mobile/inspections/{id}/evidences")
    suspend fun addEvidence(
        @Path("id") inspectionId: Long,
        @Part file: MultipartBody.Part,
        @Query("sectionId") sectionId: Long,
        @Query("comment") comment: String?,
    ): InspectionDraftDto

    @DELETE("mobile/inspections/{id}/evidences/{evidenceId}")
    suspend fun deleteEvidence(
        @Path("id") inspectionId: Long,
        @Path("evidenceId") evidenceId: Long,
    ): InspectionDraftDto

    @POST("mobile/inspections/{id}/pause")
    suspend fun pauseInspection(@Path("id") inspectionId: Long): InspectionDraftDto

    @DELETE("mobile/inspections/{id}")
    suspend fun abandonInspection(@Path("id") inspectionId: Long)

    @POST("mobile/inspections/{id}/submit")
    suspend fun submitInspection(@Path("id") inspectionId: Long)

    @GET("mobile/inspections/history")
    suspend fun listCompletedInspections(): List<InspectionHistoryDto>

    @POST("mobile/evaluacion")
    suspend fun submitEvaluacion(@Body request: MobileEvaluacionRequestDto)
}
