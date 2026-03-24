package com.sivemore.mobile.domain.model

data class AuthCredentials(
    val username: String,
    val password: String,
)

data class AuthenticatedUser(
    val id: Long,
    val username: String,
    val fullName: String,
    val role: String,
)
