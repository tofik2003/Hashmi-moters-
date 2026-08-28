package com.hashmimotors.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.hashmimotors.app.ui.HashmiMotorsMainScreen
import com.hashmimotors.app.ui.theme.HashmiMotorsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HashmiMotorsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HashmiMotorsMainScreen()
                }
            }
        }
    }
}
