package com.hashmimotors.app.ui.sound

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Holds the app-wide feedback services (sound + haptics) so any composable
 * can produce tactile/audio responses without a ViewModel.
 */
data class AppFeedback(
    val haptic: HapticManager,
    val sound: SoundManager
)

val LocalAppFeedback = staticCompositionLocalOf<AppFeedback?> { null }

/** Convenience accessors for common interactions. */
object Feedback {
    fun tap(feedback: AppFeedback?) {
        feedback?.haptic?.lightTap()
        feedback?.sound?.play(SoundEffect.TAP)
    }

    fun scan(feedback: AppFeedback?) {
        feedback?.haptic?.heavyClick()
        feedback?.sound?.play(SoundEffect.NOTIFICATION)
    }

    fun success(feedback: AppFeedback?) {
        feedback?.haptic?.success()
        feedback?.sound?.play(SoundEffect.SUCCESS)
    }

    fun error(feedback: AppFeedback?) {
        feedback?.haptic?.error()
        feedback?.sound?.play(SoundEffect.ERROR)
    }

    fun billSaved(feedback: AppFeedback?) {
        feedback?.haptic?.success()
        feedback?.sound?.play(SoundEffect.BILL_SAVED)
    }
}
