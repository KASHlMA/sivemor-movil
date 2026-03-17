package com.sivemore.mobile

import com.sivemore.mobile.data.fixtures.FakeCatalog
import com.sivemore.mobile.domain.model.ProfileSummary
import com.sivemore.mobile.domain.repository.ProfileRepository
import com.sivemore.mobile.feature.profile.ProfileEvent
import com.sivemore.mobile.feature.profile.ProfileUiAction
import com.sivemore.mobile.feature.profile.ProfileViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initLoadsProfile() = runTest {
        val viewModel = ProfileViewModel(
            profileRepository = StaticProfileRepository(summary = FakeCatalog.profile),
        )

        advanceUntilIdle()

        assertEquals(FakeCatalog.profile, viewModel.uiState.value.summary)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun initShowsEmptyStateWhenRepositoryReturnsNull() = runTest {
        val viewModel = ProfileViewModel(
            profileRepository = StaticProfileRepository(summary = null),
        )

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.summary)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun signOutEmitsSignedOutEvent() = runTest {
        val viewModel = ProfileViewModel(
            profileRepository = StaticProfileRepository(summary = FakeCatalog.profile),
        )
        advanceUntilIdle()

        val event = async { viewModel.events.first() }
        viewModel.onAction(ProfileUiAction.SignOut)
        advanceUntilIdle()

        assertEquals(ProfileEvent.SignedOut, event.await())
    }

    @Test
    fun initStaysLoadingWhileProfileRequestRuns() = runTest {
        val viewModel = ProfileViewModel(
            profileRepository = StaticProfileRepository(
                summary = FakeCatalog.profile,
                delayMs = 1_000,
            ),
        )

        assertTrue(viewModel.uiState.value.isLoading)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    private class StaticProfileRepository(
        private val summary: ProfileSummary?,
        private val delayMs: Long = 0,
    ) : ProfileRepository {
        override suspend fun loadProfile(): ProfileSummary? {
            delay(delayMs)
            return summary
        }
    }
}
