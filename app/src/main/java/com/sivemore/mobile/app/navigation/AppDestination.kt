package com.sivemore.mobile.app.navigation

import androidx.annotation.StringRes
import com.sivemore.mobile.R

sealed class AppDestination(
    val route: String,
    @StringRes val labelRes: Int,
) {
    data object Auth : AppDestination(route = "auth", labelRes = R.string.app_name)
    data object Home : AppDestination(route = "home", labelRes = R.string.home_route)
    data object Profile : AppDestination(route = "profile", labelRes = R.string.profile_route)

    companion object {
        val bottomBarDestinations = listOf(Home, Profile)

        fun fromRoute(route: String?): AppDestination? = when (route) {
            Auth.route -> Auth
            Home.route -> Home
            Profile.route -> Profile
            else -> null
        }
    }
}

