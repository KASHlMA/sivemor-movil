package com.sivemore.mobile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class MainNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun authFlowNavigatesAcrossVerificationStates() {
        composeRule.onNodeWithTag("auth_email").performTextInput("inspector@sivemor.mx")
        composeRule.onNodeWithTag("auth_password").performTextInput("secret")
        composeRule.onNodeWithTag("auth_continue").performClick()

        composeRule.onNodeWithTag("vehicle_lookup_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("vehicle_card_veh-003").performClick()
        composeRule.onNodeWithText("Continuar").performClick()

        composeRule.onNodeWithTag("verification_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("item_tires_missing_lugs_option_missing").performClick()
        composeRule.onNodeWithTag("action_add_evidence").performClick()
        composeRule.onNodeWithTag("source_camera").performClick()
        composeRule.onNodeWithTag("header_action").performClick()

        composeRule.onNodeWithTag("session_actions_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("pause_session_button").performClick()
        composeRule.onNodeWithText("Pausar").performClick()

        composeRule.onNodeWithTag("vehicle_lookup_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("vehicle_card_veh-003").performClick()
        composeRule.onNodeWithText("Continuar").performClick()
        composeRule.onNodeWithTag("header_action").performClick()
        composeRule.onNodeWithTag("logout_session_button").performClick()
        composeRule.onNodeWithText("Salir").performClick()
        composeRule.onNodeWithTag("auth_screen").assertIsDisplayed()
    }
}
