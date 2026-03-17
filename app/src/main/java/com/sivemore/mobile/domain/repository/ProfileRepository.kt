package com.sivemore.mobile.domain.repository

import com.sivemore.mobile.domain.model.ProfileSummary

interface ProfileRepository {
    suspend fun loadProfile(): ProfileSummary?
}

