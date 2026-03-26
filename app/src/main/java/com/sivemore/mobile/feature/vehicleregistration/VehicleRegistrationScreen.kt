package com.sivemore.mobile.feature.vehicleregistration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sivemore.mobile.R
import com.sivemore.mobile.app.designsystem.BrandedHeader
import com.sivemore.mobile.app.designsystem.LoginFooterDecoration
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.app.designsystem.SivemoreThemeTokens
import com.sivemore.mobile.preview.PhonePreview

@Composable
fun VehicleRegistrationScreen(
    modifier: Modifier = Modifier,
) {
    val spacing = SivemoreThemeTokens.spacing

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("vehicle_registration_screen"),
    ) {
        BrandedHeader(
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 32.dp, top = 138.dp, end = 32.dp, bottom = 176.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                Text(
                    text = stringResource(R.string.registration_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("vehicle_registration_title"),
                )
                Text(
                    text = stringResource(R.string.registration_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        LoginFooterDecoration(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(144.dp),
        )
    }
}

@PhonePreview
@Composable
private fun VehicleRegistrationScreenPreview() {
    SivemoreTheme {
        VehicleRegistrationScreen()
    }
}
