package com.sivemore.mobile.domain.repository

import com.sivemore.mobile.domain.model.AuthCredentials
import com.sivemore.mobile.domain.model.AuthenticatedUser

interface AuthRepository {
    suspend fun signIn(credentials: AuthCredentials): Result<AuthenticatedUser>
    suspend fun signOut()
    fun hasActiveSession(): Boolean
    fun currentUser(): AuthenticatedUser?
}
