package com.ssnlva

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ssnlva.ui.piano.PianoScreen
import com.ssnlva.ui.theme.PiAInoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PiAInoTheme {
                PianoScreen()
            }
        }
    }
}