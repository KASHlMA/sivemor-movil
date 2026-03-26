package com.sivemore.mobile

import com.sivemore.mobile.domain.model.AuthCredentials
import com.sivemore.mobile.domain.model.AuthenticatedUser
import com.sivemore.mobile.domain.repository.AuthRepository
import com.sivemore.mobile.feature.auth.AuthEvent
import com.sivemore.mobile.feature.auth.AuthUiAction
import com.sivemore.mobile.feature.auth.AuthViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun submitWithBlankFieldsShowsValidationError() = runTest {
        val viewModel = AuthViewModel(authRepository = SuccessAuthRepository())

        viewModel.onAction(AuthUiAction.Submit)
        advanceUntilIdle()

        assertEquals(
            "Ingresa usuario y contraseña para continuar.",
            viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun submitWithCredentialsEmitsAuthenticatedEvent() = runTest {
        val viewModel = AuthViewModel(authRepository = SuccessAuthRepository())
        val event = async { viewModel.events.first() }

        viewModel.onAction(AuthUiAction.UsernameChanged("tecnico1"))
        viewModel.onAction(AuthUiAction.PasswordChanged("secret"))
        viewModel.onAction(AuthUiAction.Submit)
        advanceUntilIdle()

        assertEquals(AuthEvent.Authenticated, event.await())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun existingSessionImmediatelyAuthenticates() = runTest {
        val viewModel = AuthViewModel(authRepository = SuccessAuthRepository(hasSession = true))

        assertEquals(AuthEvent.Authenticated, viewModel.events.first())
    }

    @Test
    fun submitKeepsLoadingTrueWhileRepositoryIsInFlight() = runTest {
        val repository = BlockingAuthRepository()
        val viewModel = AuthViewModel(authRepository = repository)

        viewModel.onAction(AuthUiAction.UsernameChanged("tecnico1"))
        viewModel.onAction(AuthUiAction.PasswordChanged("secret"))
        viewModel.onAction(AuthUiAction.Submit)
        runCurrent()

        assertTrue(viewModel.uiState.value.isLoading)

        repository.response.complete(Result.success(sampleUser))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun submitWithRepositoryFailureShowsInlineError() = runTest {
        val viewModel = AuthViewModel(
            authRepository = FailingAuthRepository(
                message = "Esta aplicacion solo permite acceso a tecnicos.",
            ),
        )

        viewModel.onAction(AuthUiAction.UsernameChanged("admin"))
        viewModel.onAction(AuthUiAction.PasswordChanged("Admin123!"))
        viewModel.onAction(AuthUiAction.Submit)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(
            "Esta aplicacion solo permite acceso a tecnicos.",
            viewModel.uiState.value.errorMessage,
        )
        assertNull(viewModel.uiState.value.diagnosticMessage)
    }

    private class SuccessAuthRepository(
        private val hasSession: Boolean = false,
    ) : AuthRepository {
        override suspend fun signIn(credentials: AuthCredentials): Result<AuthenticatedUser> =
            Result.success(sampleUser.copy(username = credentials.username))

        override suspend fun probeBackend(): Result<String> = Result.success("Health OK")

        override suspend fun signOut() = Unit

        override fun hasActiveSession(): Boolean = hasSession

        override fun currentUser(): AuthenticatedUser? = sampleUser.takeIf { hasSession }
    }

    private class BlockingAuthRepository : AuthRepository {
        val response = CompletableDeferred<Result<AuthenticatedUser>>()

        override suspend fun signIn(credentials: AuthCredentials): Result<AuthenticatedUser> = response.await()

        override suspend fun probeBackend(): Result<String> = Result.success("Health OK")

        override suspend fun signOut() = Unit

        override fun hasActiveSession(): Boolean = false

        override fun currentUser(): AuthenticatedUser? = null
    }

    private class FailingAuthRepository(
        private val message: String,
    ) : AuthRepository {
        override suspend fun signIn(credentials: AuthCredentials): Result<AuthenticatedUser> =
            Result.failure(IllegalStateException(message))

        override suspend fun probeBackend(): Result<String> = Result.success("Health OK")

        override suspend fun signOut() = Unit

        override fun hasActiveSession(): Boolean = false

        override fun currentUser(): AuthenticatedUser? = null
    }
}
