package com.sivemore.mobile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.data.fixtures.FakeCatalog
import com.sivemore.mobile.feature.home.HomeScreen
import com.sivemore.mobile.feature.home.HomeUiState
import com.sivemore.mobile.feature.profile.ProfileScreen
import com.sivemore.mobile.feature.profile.ProfileUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenshotRegressionTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun captureHomeLoadedState() {
        composeRule.setContent {
            SivemoreTheme {
                HomeScreen(
                    state = HomeUiState(
                        isLoading = false,
                        overview = FakeCatalog.overview,
                    ),
                    onAction = {},
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun captureProfileLoadedState() {
        composeRule.setContent {
            SivemoreTheme {
                ProfileScreen(
                    state = ProfileUiState(
                        isLoading = false,
                        summary = FakeCatalog.profile,
                    ),
                    onAction = {},
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage()
    }
}

