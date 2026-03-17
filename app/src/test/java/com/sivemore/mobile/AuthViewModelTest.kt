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
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun submitWithBlankFieldsShowsValidationError() = runTest {
        val viewModel = AuthViewModel(
            authRepository = SuccessAuthRepository(),
        )

        viewModel.onAction(AuthUiAction.Submit)
        advanceUntilIdle()

        assertEquals(
            "Enter an email and password to continue.",
            viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun submitWithCredentialsEmitsAuthenticatedEvent() = runTest {
        val viewModel = AuthViewModel(
            authRepository = SuccessAuthRepository(),
        )
        val event = async { viewModel.events.first() }

        viewModel.onAction(AuthUiAction.EmailChanged("team@sivemore.app"))
        viewModel.onAction(AuthUiAction.PasswordChanged("secret"))
        viewModel.onAction(AuthUiAction.Submit)
        advanceUntilIdle()

        assertEquals(AuthEvent.Authenticated, event.await())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun submitKeepsLoadingTrueWhileRepositoryIsInFlight() = runTest {
        val repository = BlockingAuthRepository()
        val viewModel = AuthViewModel(authRepository = repository)

        viewModel.onAction(AuthUiAction.EmailChanged("team@sivemore.app"))
        viewModel.onAction(AuthUiAction.PasswordChanged("secret"))
        viewModel.onAction(AuthUiAction.Submit)

        assertTrue(viewModel.uiState.value.isLoading)

        repository.response.complete(Result.success(SAMPLE_USER))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    private class SuccessAuthRepository : AuthRepository {
        override suspend fun signIn(credentials: AuthCredentials): Result<AuthenticatedUser> {
            return Result.success(SAMPLE_USER.copy(email = credentials.email))
        }

        override suspend fun continueAsGuest(): AuthenticatedUser = SAMPLE_USER
    }

    private class BlockingAuthRepository : AuthRepository {
        val response = CompletableDeferred<Result<AuthenticatedUser>>()

        override suspend fun signIn(credentials: AuthCredentials): Result<AuthenticatedUser> {
            return response.await()
        }

        override suspend fun continueAsGuest(): AuthenticatedUser = SAMPLE_USER
    }

    companion object {
        private val SAMPLE_USER = AuthenticatedUser(
            id = "1",
            displayName = "Sofia Benitez",
            email = "team@sivemore.app",
        )
    }
}
