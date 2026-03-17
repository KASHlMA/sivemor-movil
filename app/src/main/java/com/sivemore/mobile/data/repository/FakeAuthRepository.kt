package com.sivemore.mobile.data.repository

import com.sivemore.mobile.data.fixtures.FakeCatalog
import com.sivemore.mobile.domain.model.AuthCredentials
import com.sivemore.mobile.domain.model.AuthenticatedUser
import com.sivemore.mobile.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {

    override suspend fun signIn(credentials: AuthCredentials): Result<AuthenticatedUser> {
        return Result.success(
            FakeCatalog.user.copy(email = credentials.email),
        )
    }

    override suspend fun continueAsGuest(): AuthenticatedUser {
        return FakeCatalog.user.copy(
            id = "guest",
            displayName = "Guest explorer",
            email = "guest@sivemore.app",
        )
    }
}
