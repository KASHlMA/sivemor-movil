package com.sivemore.mobile.domain.repository

import com.sivemore.mobile.domain.model.HomeOverview

interface HomeRepository {
    suspend fun loadOverview(): HomeOverview?
}

