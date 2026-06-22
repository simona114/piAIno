package com.ssnlva

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ssnlva.ui.navigation.PianoRoute
import com.ssnlva.ui.navigation.SettingsRoute
import com.ssnlva.ui.piano.PianoScreen
import com.ssnlva.ui.piano.PianoViewModel
import com.ssnlva.ui.settings.SettingsScreen
import com.ssnlva.ui.settings.SettingsViewModel
import com.ssnlva.ui.theme.PiAInoTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PiAInoTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = PianoRoute) {
                    composable<PianoRoute> {
                        val pianoViewModel: PianoViewModel = koinViewModel()
                        LaunchedEffect(Unit) { pianoViewModel.refreshPreferences() }
                        PianoScreen(
                            showNoteNames = pianoViewModel.showNoteNames,
                            notation = pianoViewModel.notation,
                            sustainEnabled = pianoViewModel.sustainEnabled,
                            onSettingsClick = { navController.navigate(SettingsRoute) },
                            onSustainToggle = pianoViewModel::onSustainToggle,
                            onKeyPressed = pianoViewModel::onKeyPressed,
                            onKeyReleased = pianoViewModel::onKeyReleased
                        )
                    }
                    composable<SettingsRoute> {
                        val settingsViewModel: SettingsViewModel = koinViewModel()
                        SettingsScreen(
                            showNoteNames = settingsViewModel.showNoteNames,
                            onShowNoteNamesChange = settingsViewModel::updateShowNoteNames,
                            notation = settingsViewModel.notation,
                            onNotationChange = settingsViewModel::updateNotation,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
