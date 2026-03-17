package com.sivemore.mobile

import com.sivemore.mobile.data.fixtures.FakeCatalog
import com.sivemore.mobile.domain.model.HomeOverview
import com.sivemore.mobile.domain.repository.HomeRepository
import com.sivemore.mobile.feature.home.HomeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initLoadsDashboardContent() = runTest {
        val viewModel = HomeViewModel(
            homeRepository = StaticHomeRepository(overview = FakeCatalog.overview),
        )

        advanceUntilIdle()

        assertEquals(FakeCatalog.overview, viewModel.uiState.value.overview)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun initStaysLoadingUntilRepositoryReturns() = runTest {
        val viewModel = HomeViewModel(
            homeRepository = StaticHomeRepository(
                overview = FakeCatalog.overview,
                delayMs = 1_000,
            ),
        )

        assertTrue(viewModel.uiState.value.isLoading)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun initShowsEmptyStateWhenRepositoryReturnsNull() = runTest {
        val viewModel = HomeViewModel(
            homeRepository = StaticHomeRepository(overview = null),
        )

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.overview)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    private class StaticHomeRepository(
        private val overview: HomeOverview?,
        private val delayMs: Long = 0,
    ) : HomeRepository {
        override suspend fun loadOverview(): HomeOverview? {
            delay(delayMs)
            return overview
        }
    }
}

