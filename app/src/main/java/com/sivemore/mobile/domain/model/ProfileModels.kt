package com.sivemore.mobile.domain.model

data class ProfilePill(
    val label: String,
)

data class ProfileSetting(
    val title: String,
    val value: String,
)

data class ProfileSummary(
    val displayName: String,
    val role: String,
    val email: String,
    val city: String,
    val completion: Int,
    val focusAreas: List<ProfilePill>,
    val settings: List<ProfileSetting>,
)

