package com.sivemore.mobile.data.network

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequestDto): AuthResponseDto

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequestDto)
}
