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
import com.sivemore.mobile.feature.inspection.AireFrenosRoute
import com.sivemore.mobile.feature.inspection.DireccionRoute
import com.sivemore.mobile.feature.inspection.EvidenciasRoute
import com.sivemore.mobile.feature.inspection.InspectionNextSectionRoute
import com.sivemore.mobile.feature.inspection.InspectionFlowViewModel
import com.sivemore.mobile.feature.inspection.LlantasRoute
import com.sivemore.mobile.feature.inspection.MotorRoute
import com.sivemore.mobile.feature.inspection.OtrosRoute
import com.sivemore.mobile.feature.luces.LucesRoute
import com.sivemore.mobile.feature.sessionactions.SessionActionsRoute
import com.sivemore.mobile.feature.vehiclemenu.VehicleMenuRoute
import com.sivemore.mobile.feature.vehiclelookup.VehicleLookupRoute
import com.sivemore.mobile.feature.vehicleregistration.VehicleRegistrationRoute
import com.sivemore.mobile.feature.reports.ReportsRoute
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
                    navController.navigate(AppDestination.VehicleRegistration.createRoute()) {
                        launchSingleTop = true
                    }
                },
                onOpenReports = {
                    navController.navigate(AppDestination.Reports.route) {
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
        composable(AppDestination.Reports.route) {
            ReportsRoute(
                onNavigateBack = {
                    navController.popBackStack(
                        route = AppDestination.VehicleMenu.route,
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
        composable(AppDestination.VehicleLookup.route) {
            VehicleLookupRoute(
                onNavigateBack = {
                    navController.popBackStack(
                        route = AppDestination.VehicleMenu.route,
                        inclusive = false,
                    )
                },
                onOpenVerification = { vehicleId ->
                    navController.navigate(AppDestination.VerificationFlow.createRoute(vehicleId))
                },
                onEditVehicle = { vehicleId ->
                    navController.navigate(AppDestination.VehicleRegistration.createRoute(vehicleId)) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(
            route = AppDestination.VehicleRegistration.route,
            arguments = listOf(
                navArgument("vehicleId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            VehicleRegistrationRoute(
                onBackToMenu = {
                    navController.popBackStack(
                        route = AppDestination.VehicleMenu.route,
                        inclusive = false,
                    )
                },
                onVehicleSaved = { vehicleId ->
                    navController.navigate(AppDestination.VerificationFlow.createRoute(vehicleId))
                },
                onSignedOut = {
                    navController.navigate(AppDestination.Auth.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
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
                        navController.navigate(AppDestination.Llantas.route)
                    },
                    onNavigateBack = {
                        navController.popBackStack(
                            route = AppDestination.VehicleLookup.route,
                            inclusive = false,
                        )
                    },
                    onBackToLookup = {
                        navController.popBackStack(
                            route = AppDestination.VehicleLookup.route,
                            inclusive = false,
                        )
                    },
                    onBackToMenu = {
                        navController.navigate(AppDestination.VehicleMenu.route) {
                            popUpTo(AppDestination.VehicleMenu.route) { inclusive = false }
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
            composable(AppDestination.Llantas.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppDestination.VerificationFlow.route)
                }
                val viewModel: InspectionFlowViewModel = hiltViewModel(parentEntry)
                LlantasRoute(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateNext = {
                        navController.navigate(AppDestination.Direccion.route)
                    },
                    onBackToLookup = {
                        navController.popBackStack(
                            route = AppDestination.VehicleLookup.route,
                            inclusive = false,
                        )
                    },
                    onBackToMenu = {
                        navController.navigate(AppDestination.VehicleMenu.route) {
                            popUpTo(AppDestination.VehicleMenu.route) { inclusive = false }
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
            composable(AppDestination.Direccion.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppDestination.VerificationFlow.route)
                }
                val viewModel: InspectionFlowViewModel = hiltViewModel(parentEntry)
                DireccionRoute(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateNext = {
                        navController.navigate(AppDestination.AireFrenos.route)
                    },
                    onBackToLookup = {
                        navController.popBackStack(
                            route = AppDestination.VehicleLookup.route,
                            inclusive = false,
                        )
                    },
                    onBackToMenu = {
                        navController.navigate(AppDestination.VehicleMenu.route) {
                            popUpTo(AppDestination.VehicleMenu.route) { inclusive = false }
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
            composable(AppDestination.AireFrenos.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppDestination.VerificationFlow.route)
                }
                val viewModel: InspectionFlowViewModel = hiltViewModel(parentEntry)
                AireFrenosRoute(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateNext = {
                        navController.navigate(AppDestination.Motor.route)
                    },
                    onBackToLookup = {
                        navController.popBackStack(
                            route = AppDestination.VehicleLookup.route,
                            inclusive = false,
                        )
                    },
                    onBackToMenu = {
                        navController.navigate(AppDestination.VehicleMenu.route) {
                            popUpTo(AppDestination.VehicleMenu.route) { inclusive = false }
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
            composable(AppDestination.Motor.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppDestination.VerificationFlow.route)
                }
                val viewModel: InspectionFlowViewModel = hiltViewModel(parentEntry)
                MotorRoute(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateNext = {
                        navController.navigate(AppDestination.Otros.route)
                    },
                    onBackToLookup = {
                        navController.popBackStack(
                            route = AppDestination.VehicleLookup.route,
                            inclusive = false,
                        )
                    },
                    onBackToMenu = {
                        navController.navigate(AppDestination.VehicleMenu.route) {
                            popUpTo(AppDestination.VehicleMenu.route) { inclusive = false }
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
            composable(AppDestination.Otros.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppDestination.VerificationFlow.route)
                }
                val viewModel: InspectionFlowViewModel = hiltViewModel(parentEntry)
                OtrosRoute(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateNext = {
                        navController.navigate(AppDestination.Evidencias.route)
                    },
                    onBackToLookup = {
                        navController.popBackStack(
                            route = AppDestination.VehicleLookup.route,
                            inclusive = false,
                        )
                    },
                    onBackToMenu = {
                        navController.navigate(AppDestination.VehicleMenu.route) {
                            popUpTo(AppDestination.VehicleMenu.route) { inclusive = false }
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
            composable(AppDestination.Evidencias.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppDestination.VerificationFlow.route)
                }
                val viewModel: InspectionFlowViewModel = hiltViewModel(parentEntry)
                EvidenciasRoute(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onBackToMenu = {
                        navController.navigate(AppDestination.VehicleMenu.route) {
                            popUpTo(AppDestination.VehicleMenu.route) { inclusive = false }
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
                    onBackToMenu = {
                        navController.navigate(AppDestination.VehicleMenu.route) {
                            popUpTo(AppDestination.VehicleMenu.route) { inclusive = false }
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
                onBackToMenu = {
                    navController.navigate(AppDestination.VehicleMenu.route) {
                        popUpTo(AppDestination.VehicleMenu.route) { inclusive = false }
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
        composable(
            route = AppDestination.SessionActions.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType }),
        ) {
            SessionActionsRoute(
                onBackToMenu = {
                    navController.navigate(AppDestination.VehicleMenu.route) {
                        popUpTo(AppDestination.VehicleMenu.route) { inclusive = false }
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
    }
}
