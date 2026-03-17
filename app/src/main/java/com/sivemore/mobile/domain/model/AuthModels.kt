package com.sivemore.mobile.domain.model

data class AuthCredentials(
    val email: String,
    val password: String,
)

data class AuthenticatedUser(
    val id: String,
    val displayName: String,
    val email: String,
)

