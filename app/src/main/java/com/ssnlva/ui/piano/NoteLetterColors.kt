package com.ssnlva.ui.piano

import androidx.compose.ui.graphics.Color

// internal (not private) so tests can verify every white key's letter has a registered color.
internal val NoteLetterColors: Map<String, Color> = mapOf(
    "A" to Color(0xFF6A1B9A), // purple
    "B" to Color(0xFF1E88E5), // blue
    "C" to Color(0xFF2E7D32), // green
    "D" to Color(0xFFF9A825), // yellow
    "E" to Color(0xFFEF6C00), // orange
    "F" to Color(0xFFC62828), // red
    "G" to Color(0xFFD81B60), // pink
)
