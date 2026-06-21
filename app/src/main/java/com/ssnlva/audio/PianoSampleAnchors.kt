package com.ssnlva.audio

import kotlin.math.abs
import kotlin.math.pow

/** How to play a requested note from an already-sampled anchor: which anchor's recording to
 *  use ([anchorMidiNote]) and the SoundPool playback rate ([rate]) that pitch-shifts it from
 *  that anchor's natural pitch to the requested one. */
data class ResolvedSample(val anchorMidiNote: Int, val rate: Float)

/** The 30 MIDI notes with a real recorded sample, spaced every 3 semitones (minor thirds)
 *  across the keyboard's full A0-C8 range, matching the Salamander Grand Piano's own
 *  native sampling (see app/src/main/assets/CREDITS.md). */
object PianoSampleAnchors {
    val anchorMidiNotes: List<Int> = (21..108 step 3).toList()

    /** Nearest anchor to [midiNote] and the SoundPool playback rate needed to sound at
     *  [midiNote] from that anchor's own natural pitch. Never more than 1 semitone away
     *  given the 3-semitone anchor spacing, so rate is always well inside SoundPool's
     *  [0.5, 2.0] clamp range. */
    fun resolve(midiNote: Int): ResolvedSample {
        val nearest = anchorMidiNotes.minBy { abs(it - midiNote) }
        val rate = 2.0.pow((midiNote - nearest) / 12.0).toFloat()
        return ResolvedSample(nearest, rate)
    }
}
