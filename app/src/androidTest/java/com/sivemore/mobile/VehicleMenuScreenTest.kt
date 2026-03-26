package com.sivemore.mobile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.feature.vehiclemenu.VehicleMenuScreen
import com.sivemore.mobile.feature.vehiclemenu.VehicleMenuUiAction
import com.sivemore.mobile.feature.vehiclemenu.VehicleMenuUiState
import com.sivemore.mobile.feature.vehicleregistration.VehicleRegistrationScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class VehicleMenuScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun vehicleMenuShowsTitleAndActions() {
        composeRule.setContent {
            SivemoreTheme {
                VehicleMenuScreen(
                    state = VehicleMenuUiState(),
                    onAction = {},
                )
            }
        }

        composeRule.onNodeWithTag("vehicle_menu_title").assertIsDisplayed()
        composeRule.onNodeWithTag("vehicle_menu_visualization").assertIsDisplayed()
        composeRule.onNodeWithTag("vehicle_menu_registration").assertIsDisplayed()
    }

    @Test
    fun vehicleMenuCallbacksAreTriggered() {
        val actions = mutableListOf<VehicleMenuUiAction>()

        composeRule.setContent {
            SivemoreTheme {
                VehicleMenuScreen(
                    state = VehicleMenuUiState(),
                    onAction = { actions += it },
                )
            }
        }

        composeRule.onNodeWithTag("vehicle_menu_visualization").performClick()
        composeRule.onNodeWithTag("vehicle_menu_registration").performClick()
        composeRule.onNodeWithTag("header_action").performClick()

        assertEquals(
            listOf(
                VehicleMenuUiAction.OpenVisualization,
                VehicleMenuUiAction.OpenRegistration,
                VehicleMenuUiAction.SignOut,
            ),
            actions,
        )
    }

    @Test
    fun vehicleRegistrationPlaceholderShowsTitle() {
        composeRule.setContent {
            SivemoreTheme {
                VehicleRegistrationScreen()
            }
        }

        composeRule.onNodeWithTag("vehicle_registration_title").assertIsDisplayed()
    }
}
