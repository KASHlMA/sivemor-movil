package com.sivemore.mobile.data.network

import com.sivemore.mobile.data.session.SessionStore
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator @Inject constructor(
    private val sessionStore: SessionStore,
    private val authApiService: AuthApiService,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.contains("/auth/") || responseCount(response) >= 2) {
            return null
        }

        val refreshToken = sessionStore.refreshToken() ?: return null
        val refreshedSession = runBlocking {
            runCatching {
                authApiService.refresh(RefreshRequestDto(refreshToken))
            }.getOrNull()
        } ?: run {
            sessionStore.clear()
            return null
        }

        sessionStore.saveSession(
            accessToken = refreshedSession.accessToken,
            refreshToken = refreshedSession.refreshToken,
            user = refreshedSession.user.toDomain(),
        )

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${refreshedSession.accessToken}")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var current = response.priorResponse
        var count = 1
        while (current != null) {
            count++
            current = current.priorResponse
        }
        return count
    }
}
