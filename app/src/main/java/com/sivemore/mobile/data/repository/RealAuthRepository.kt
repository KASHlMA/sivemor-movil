package com.sivemore.mobile.data.repository

import com.sivemore.mobile.data.network.AuthApiService
import com.sivemore.mobile.data.network.LoginRequestDto
import com.sivemore.mobile.data.network.LogoutRequestDto
import com.sivemore.mobile.data.network.MobileErrorMapper
import com.sivemore.mobile.data.network.toDomain
import com.sivemore.mobile.data.session.SessionStore
import com.sivemore.mobile.domain.model.AuthCredentials
import com.sivemore.mobile.domain.model.AuthenticatedUser
import com.sivemore.mobile.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealAuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val sessionStore: SessionStore,
    private val errorMapper: MobileErrorMapper,
) : AuthRepository {
    override suspend fun signIn(credentials: AuthCredentials): Result<AuthenticatedUser> = runCatching {
        val response = authApiService.login(
            LoginRequestDto(
                username = credentials.username,
                password = credentials.password,
            )
        )
        val user = response.user.toDomain()
        sessionStore.saveSession(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            user = user,
        )
        user
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(IllegalStateException(errorMapper.toMessage(it))) },
    )

    override suspend fun signOut() {
        sessionStore.refreshToken()?.let { refreshToken ->
            runCatching { authApiService.logout(LogoutRequestDto(refreshToken)) }
        }
        sessionStore.clear()
    }

    override fun hasActiveSession(): Boolean = sessionStore.hasActiveSession()

    override fun currentUser(): AuthenticatedUser? = sessionStore.currentUser()
}
