package com.sivemore.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.sivemore.mobile.app.navigation.SivemoreApp
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SivemoreTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SivemoreApp()
                }
            }
        }
    }
}

