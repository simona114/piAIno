package com.ssnlva.domain.piano

data class PianoKey(val isBlack: Boolean)

object PianoOctaveLayout {
    val keys: List<PianoKey> = listOf(
        PianoKey(isBlack = false), // C
        PianoKey(isBlack = true),  // C#
        PianoKey(isBlack = false), // D
        PianoKey(isBlack = true),  // D#
        PianoKey(isBlack = false), // E
        PianoKey(isBlack = false), // F
        PianoKey(isBlack = true),  // F#
        PianoKey(isBlack = false), // G
        PianoKey(isBlack = true),  // G#
        PianoKey(isBlack = false), // A
        PianoKey(isBlack = true),  // A#
        PianoKey(isBlack = false), // B
    )
}
