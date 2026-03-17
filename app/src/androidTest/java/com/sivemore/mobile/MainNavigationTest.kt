package com.sivemore.mobile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class MainNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun authFlowNavigatesAcrossMainScreens() {
        composeRule.onNodeWithTag("auth_email").performTextInput("team@sivemore.app")
        composeRule.onNodeWithTag("auth_password").performTextInput("secret")
        composeRule.onNodeWithTag("auth_continue").performClick()

        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("tab_profile").performClick()
        composeRule.onNodeWithTag("profile_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("profile_logout").performClick()
        composeRule.onNodeWithTag("auth_screen").assertIsDisplayed()
    }
}
