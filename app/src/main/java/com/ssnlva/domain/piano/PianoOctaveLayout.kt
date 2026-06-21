package com.ssnlva.domain.piano

data class PianoKey(val name: String, val isBlack: Boolean)

object PianoOctaveLayout {
    val keys: List<PianoKey> = listOf(
        PianoKey(name = "C", isBlack = false),
        PianoKey(name = "C#", isBlack = true),
        PianoKey(name = "D", isBlack = false),
        PianoKey(name = "D#", isBlack = true),
        PianoKey(name = "E", isBlack = false),
        PianoKey(name = "F", isBlack = false),
        PianoKey(name = "F#", isBlack = true),
        PianoKey(name = "G", isBlack = false),
        PianoKey(name = "G#", isBlack = true),
        PianoKey(name = "A", isBlack = false),
        PianoKey(name = "A#", isBlack = true),
        PianoKey(name = "B", isBlack = false),
    )
}
