package com.sivemore.mobile.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sivemore.mobile.feature.auth.AuthRoute
import com.sivemore.mobile.feature.home.HomeRoute
import com.sivemore.mobile.feature.profile.ProfileRoute

@Composable
fun SivemoreNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Auth.route,
        modifier = modifier,
    ) {
        composable(AppDestination.Auth.route) {
            AuthRoute(
                onAuthenticated = {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Auth.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(AppDestination.Home.route) {
            HomeRoute()
        }
        composable(AppDestination.Profile.route) {
            ProfileRoute(
                onSignedOut = {
                    navController.navigate(AppDestination.Auth.route) {
                        popUpTo(AppDestination.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}

