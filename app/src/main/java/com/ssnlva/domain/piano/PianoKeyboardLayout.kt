package com.ssnlva.domain.piano

data class PianoKey(val name: String, val octave: Int, val isBlack: Boolean)

val PianoKey.displayName: String get() = "$name$octave"

object PianoKeyboardLayout {
    private const val FirstMidiNote = 21 // A0
    private const val LastMidiNote = 108 // C8

    private val ChromaticPattern: List<Pair<String, Boolean>> = listOf(
        "C" to false,
        "C#" to true,
        "D" to false,
        "D#" to true,
        "E" to false,
        "F" to false,
        "F#" to true,
        "G" to false,
        "G#" to true,
        "A" to false,
        "A#" to true,
        "B" to false,
    )

    val keys: List<PianoKey> = (FirstMidiNote..LastMidiNote).map { midiNote ->
        val (name, isBlack) = ChromaticPattern[midiNote % 12]
        val octave = midiNote / 12 - 1
        PianoKey(name = name, octave = octave, isBlack = isBlack)
    }

    val centerKeyIndex: Int = keys.indexOfFirst { it.name == "C" && it.octave == 4 }
}
