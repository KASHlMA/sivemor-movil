package com.sivemore.mobile.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.sivemore.mobile.feature.auth.AuthRoute
import com.sivemore.mobile.feature.inspection.InspectionNextSectionRoute
import com.sivemore.mobile.feature.inspection.InspectionFlowViewModel
import com.sivemore.mobile.feature.luces.LucesRoute
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
                    navController.navigate(AppDestination.VerificationFlow.createRoute(vehicleId))
                },
            )
        }
        composable(AppDestination.VehicleRegistration.route) {
            VehicleRegistrationScreen()
        }
        navigation(
            route = AppDestination.VerificationFlow.route,
            startDestination = AppDestination.Luces.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType }),
        ) {
            composable(AppDestination.Luces.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppDestination.VerificationFlow.route)
                }
                val viewModel: InspectionFlowViewModel = hiltViewModel(parentEntry)
                LucesRoute(
                    viewModel = viewModel,
                    onNavigateNext = {
                        navController.navigate(AppDestination.InspectionNextSection.route)
                    },
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
            composable(AppDestination.InspectionNextSection.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppDestination.VerificationFlow.route)
                }
                val viewModel: InspectionFlowViewModel = hiltViewModel(parentEntry)
                InspectionNextSectionRoute(
                    viewModel = viewModel,
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
        composable(
            route = AppDestination.Verification.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType }),
        ) {
            VerificationRoute(
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
