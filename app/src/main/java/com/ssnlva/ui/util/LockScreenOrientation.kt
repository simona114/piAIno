package com.ssnlva.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/**
 * Locks the host Activity's orientation to landscape for as long as this composable is part of
 * the composition, restoring the previous `requestedOrientation` on dispose.
 *
 * This is a per-screen lock rather than a manifest-level `android:screenOrientation` attribute,
 * since other screens (e.g. Splash, Settings) are expected to remain portrait.
 */
@Composable
fun LockScreenOrientation(orientation: Int = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
    val activity = LocalContext.current.findActivity()
    DisposableEffect(orientation) {
        val previousOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            activity.requestedOrientation = previousOrientation
        }
    }
}

private fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("No Activity found from this Context: $this")
}
