package com.sivemore.mobile

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.data.fixtures.FakeCatalog
import com.sivemore.mobile.feature.auth.AuthScreen
import com.sivemore.mobile.feature.auth.AuthUiState
import com.sivemore.mobile.feature.sessionactions.SessionActionsScreen
import com.sivemore.mobile.feature.sessionactions.SessionActionsUiState
import com.sivemore.mobile.feature.vehiclelookup.VehicleLookupScreen
import com.sivemore.mobile.feature.vehiclelookup.VehicleLookupUiState
import com.sivemore.mobile.feature.verification.VerificationScreen
import com.sivemore.mobile.feature.verification.VerificationUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenshotRegressionTest {

    @Test
    fun captureAuthScreen() {
        captureRoboImage(filePath = "build/roborazzi/auth.png") {
            SivemoreTheme {
                AuthScreen(
                    state = AuthUiState(email = "inspector@sivemor.mx", password = "secret"),
                    onAction = {},
                )
            }
        }
    }

    @Test
    fun captureLoadingScreen() {
        captureRoboImage(filePath = "build/roborazzi/loading.png") {
            SivemoreTheme {
                AuthScreen(
                    state = AuthUiState(isLoading = true),
                    onAction = {},
                )
            }
        }
    }

    @Test
    fun captureVehicleLookupAndPendingDialog() {
        captureRoboImage(filePath = "build/roborazzi/vehicle_lookup_pending.png") {
            SivemoreTheme {
                VehicleLookupScreen(
                    state = VehicleLookupUiState(
                        isLoading = false,
                        vehicles = FakeCatalog.defaultVehicles(),
                        pendingVehicle = FakeCatalog.defaultVehicles().first(),
                    ),
                    onAction = {},
                )
            }
        }
    }

    @Test
    fun captureVerificationLightsScreen() {
        captureRoboImage(filePath = "build/roborazzi/verification_lights.png") {
            SivemoreTheme {
                VerificationScreen(
                    state = VerificationUiState(
                        isLoading = false,
                        session = FakeCatalog.createFreshSession("veh-002"),
                    ),
                    onAction = {},
                )
            }
        }
    }

    @Test
    fun captureVerificationEvidenceScreen() {
        captureRoboImage(filePath = "build/roborazzi/verification_evidence.png") {
            SivemoreTheme {
                VerificationScreen(
                    state = VerificationUiState(
                        isLoading = false,
                        session = FakeCatalog.createPendingSession("veh-003").copy(
                            selectedCategory = com.sivemore.mobile.domain.model.InspectionCategory.Evidence,
                        ),
                    ),
                    onAction = {},
                )
            }
        }
    }

    @Test
    fun capturePauseDialog() {
        captureRoboImage(filePath = "build/roborazzi/session_pause_dialog.png") {
            SivemoreTheme {
                SessionActionsScreen(
                    state = SessionActionsUiState(
                        isLoading = false,
                        vehicleLabel = "VUH-TQ8-453",
                        showPauseDialog = true,
                    ),
                    onAction = {},
                )
            }
        }
    }
}
