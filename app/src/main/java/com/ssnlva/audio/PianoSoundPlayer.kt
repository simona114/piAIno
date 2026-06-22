package com.ssnlva.audio

/**
 * Resolves a requested note (from the Piano Domain module) to playback, manages overlapping
 * voices so rapid consecutive presses don't cut off prior notes, and implements the sustain
 * mechanism: when sustain is enabled, a note's ringing voice keeps playing past [releaseNote]
 * until naturally decaying or being retriggered by [playNote].
 */
interface PianoSoundPlayer {
    /** Starts (or retriggers) the voice for [midiNote]. Never blocks; no-ops if the
     *  underlying sample isn't loaded yet. */
    fun playNote(midiNote: Int)

    /** Signals that [midiNote]'s key has been released. Stops its voice immediately unless
     *  sustain is enabled, in which case the voice keeps ringing. */
    fun releaseNote(midiNote: Int)

    /** Toggles the sustain mechanism described on [releaseNote]. */
    fun setSustainEnabled(enabled: Boolean)
}
