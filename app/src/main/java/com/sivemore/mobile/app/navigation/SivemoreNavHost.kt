package com.sivemore.mobile.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.sivemore.mobile.feature.auth.AuthRoute
import com.sivemore.mobile.feature.sessionactions.SessionActionsRoute
import com.sivemore.mobile.feature.vehiclemenu.VehicleMenuRoute
import com.sivemore.mobile.feature.vehiclelookup.VehicleLookupRoute
import com.sivemore.mobile.feature.vehicleregistration.VehicleRegistrationScreen
import com.sivemore.mobile.feature.verification.VerificationRoute

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
                    navController.navigate(AppDestination.VehicleMenu.route) {
                        popUpTo(AppDestination.Auth.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(AppDestination.VehicleMenu.route) {
            VehicleMenuRoute(
                onOpenVehicleVisualization = {
                    navController.navigate(AppDestination.VehicleLookup.route) {
                        launchSingleTop = true
                    }
                },
                onOpenVehicleRegistration = {
                    navController.navigate(AppDestination.VehicleRegistration.route) {
                        launchSingleTop = true
                    }
                },
                onSignedOut = {
                    navController.navigate(AppDestination.Auth.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(AppDestination.VehicleLookup.route) {
            VehicleLookupRoute(
                onOpenVerification = { vehicleId ->
                    navController.navigate(AppDestination.Verification.createRoute(vehicleId))
                },
            )
        }
        composable(AppDestination.VehicleRegistration.route) {
            VehicleRegistrationScreen()
        }
        composable(
            route = AppDestination.Verification.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType }),
        ) {
            VerificationRoute(
                onOpenSessionActions = { vehicleId ->
                    navController.navigate(AppDestination.SessionActions.createRoute(vehicleId))
                },
                onCompleted = {
                    navController.popBackStack()
                },
            )
        }
        composable(
            route = AppDestination.SessionActions.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType }),
        ) {
            SessionActionsRoute(
                onBackToLookup = {
                    navController.popBackStack(
                        route = AppDestination.VehicleLookup.route,
                        inclusive = false,
                    )
                },
                onSignedOut = {
                    navController.navigate(AppDestination.Auth.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}
