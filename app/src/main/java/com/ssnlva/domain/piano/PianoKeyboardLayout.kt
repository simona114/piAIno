package com.ssnlva.domain.piano

/** One key on the keyboard: its pitch class ([name]), the octave it falls in ([octave]),
 *  whether it renders as a black key, and its absolute pitch as a MIDI note number. */
data class PianoKey(val name: String, val octave: Int, val isBlack: Boolean, val midiNote: Int)

/** Conventional note label, e.g. "C#4", as printed on sheet music and used in the UI. */
val PianoKey.displayName: String get() = "$name$octave"

/** The full 88-key keyboard (A0-C8) and the chromatic naming/black-key pattern used to build it. */
object PianoKeyboardLayout {
    private const val FirstMidiNote = 21 // A0
    private const val LastMidiNote = 108 // C8

    /** Pitch class name and black-key flag for each of the 12 semitones in an octave, starting
     *  at C. Indexed by `midiNote % 12` since MIDI note numbers are C-aligned (0 = C-1). */
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

    /** Every key from [FirstMidiNote] to [LastMidiNote], in ascending pitch order. */
    val keys: List<PianoKey> = (FirstMidiNote..LastMidiNote).map { midiNote ->
        val (name, isBlack) = ChromaticPattern[midiNote % 12]
        val octave = midiNote / 12 - 1
        PianoKey(name = name, octave = octave, isBlack = isBlack, midiNote = midiNote)
    }

    /** Index into [keys] of middle C (C4), used to center the keyboard's initial scroll position. */
    val centerKeyIndex: Int = keys.indexOfFirst { it.name == "C" && it.octave == 4 }
}
