package com.sivemore.mobile.data.repository

import com.sivemore.mobile.data.fixtures.FakeCatalog
import com.sivemore.mobile.domain.model.ProfileSummary
import com.sivemore.mobile.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeProfileRepository @Inject constructor() : ProfileRepository {

    override suspend fun loadProfile(): ProfileSummary {
        return FakeCatalog.profile
    }
}
