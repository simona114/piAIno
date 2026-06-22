package com.ssnlva.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.ssnlva.R
import kotlinx.coroutines.delay

/**
 * Branded launch screen shown immediately after the system splash exits.
 * Displays the piAIno logo for 1 second then calls [onNavigateToPiano].
 *
 * The system splash (configured in themes.xml with a transparent icon) is invisible against
 * the white window background, so this composable is the first thing the user actually sees.
 *
 * @param onNavigateToPiano called once the delay elapses; the caller is responsible for
 * popping this destination off the back stack so Back cannot return to the splash.
 */
@Composable
fun SplashScreen(onNavigateToPiano: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1000)
        onNavigateToPiano()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.piaino_logo),
            contentDescription = "piAIno Logo",
            tint = Color.Unspecified,
            modifier = Modifier.fillMaxSize(0.6f)
        )
    }
}
