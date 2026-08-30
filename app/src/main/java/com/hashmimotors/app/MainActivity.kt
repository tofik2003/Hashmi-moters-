package com.hashmimotors.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.hashmimotors.app.ui.HashmiMotorsMainScreen
import com.hashmimotors.app.ui.sound.AppFeedback
import com.hashmimotors.app.ui.sound.HapticManager
import com.hashmimotors.app.ui.sound.LocalAppFeedback
import com.hashmimotors.app.ui.sound.SoundManager
import com.hashmimotors.app.ui.theme.HashmiMotorsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var hapticManager: HapticManager
    @Inject lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val feedback = AppFeedback(hapticManager, soundManager)
        setContent {
            HashmiMotorsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        LocalAppFeedback provides feedback
                    ) {
                        HashmiMotorsMainScreen()
                    }
                }
            }
        }
    }
}
