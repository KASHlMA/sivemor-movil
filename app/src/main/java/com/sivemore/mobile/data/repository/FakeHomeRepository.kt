package com.sivemore.mobile.data.repository

import com.sivemore.mobile.data.fixtures.FakeCatalog
import com.sivemore.mobile.domain.model.HomeOverview
import com.sivemore.mobile.domain.repository.HomeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeHomeRepository @Inject constructor() : HomeRepository {

    override suspend fun loadOverview(): HomeOverview {
        return FakeCatalog.overview
    }
}
