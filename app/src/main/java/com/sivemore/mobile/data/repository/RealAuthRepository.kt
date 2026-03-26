package com.sivemore.mobile.data.repository

import android.util.Log
import com.sivemore.mobile.BuildConfig
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
    override suspend fun probeBackend(): Result<String> = runCatching {
        Log.d(TAG, "Probing backend health at ${BuildConfig.API_BASE_URL} -> /actuator/health")
        val response = authApiService.healthCheck()
        "Health OK: $response"
    }.fold(
        onSuccess = {
            Log.d(TAG, it)
            Result.success(it)
        },
        onFailure = { failure ->
            logFailure("healthCheck", failure)
            Result.failure(IllegalStateException(errorMapper.toMessage(failure)))
        },
    )

    override suspend fun signIn(credentials: AuthCredentials): Result<AuthenticatedUser> = runCatching {
        Log.d(
            TAG,
            "Attempting login via ${BuildConfig.API_BASE_URL}auth/login with username=${credentials.username}",
        )
        val response = authApiService.login(
            LoginRequestDto(
                username = credentials.username,
                password = credentials.password,
            )
        )
        val user = response.user.toDomain().requireTechnicianAccess()
        sessionStore.saveSession(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            user = user,
        )
        user
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = {
            logFailure("login", it)
            Result.failure(IllegalStateException(errorMapper.toMessage(it)))
        },
    )

    override suspend fun signOut() {
        sessionStore.refreshToken()?.let { refreshToken ->
            runCatching { authApiService.logout(LogoutRequestDto(refreshToken)) }
        }
        sessionStore.clear()
    }

    override fun hasActiveSession(): Boolean {
        if (!sessionStore.hasActiveSession()) return false
        val user = sessionStore.currentUser()
        if (user?.role != TECHNICIAN_ROLE) {
            sessionStore.clear()
            return false
        }
        return true
    }

    override fun currentUser(): AuthenticatedUser? =
        sessionStore.currentUser()?.takeIf { it.role == TECHNICIAN_ROLE }

    private fun logFailure(action: String, throwable: Throwable) {
        Log.e(
            TAG,
            "Auth action '$action' failed. type=${throwable::class.java.simpleName} message=${throwable.message}",
            throwable,
        )
    }

    private fun AuthenticatedUser.requireTechnicianAccess(): AuthenticatedUser {
        if (role == TECHNICIAN_ROLE) return this
        sessionStore.clear()
        throw IllegalStateException(
            "Esta aplicacion solo permite acceso a tecnicos. Inicia sesion con tu usuario tecnico asignado.",
        )
    }

    private companion object {
        private const val TAG = "RealAuthRepository"
        private const val TECHNICIAN_ROLE = "TECHNICIAN"
    }
}
