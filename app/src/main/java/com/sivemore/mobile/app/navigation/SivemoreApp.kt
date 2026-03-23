package com.sivemore.mobile.app.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.rememberNavController

@Composable
fun SivemoreApp() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        SivemoreNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
