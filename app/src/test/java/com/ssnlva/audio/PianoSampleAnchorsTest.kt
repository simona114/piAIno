package com.ssnlva.audio

import com.ssnlva.domain.piano.PianoKeyboardLayout
import kotlin.math.pow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies [PianoSampleAnchors]' pitch-resolution math in isolation, by MIDI note number
 * rather than real [com.ssnlva.domain.piano.PianoKey]s - nearest-anchor selection, the
 * resulting playback rate, and the shape of the anchor table itself. For coverage through
 * actual white/black keys on the keyboard, see [PianoKeyPlaybackTest].
 */
class PianoSampleAnchorsTest {

    @Test
    fun `exact anchor keys resolve to themselves at rate one`() {
        for (anchor in listOf(21, 60, 108, 36, 75)) {
            val resolved = PianoSampleAnchors.resolve(anchor)
            assertEquals(anchor, resolved.anchorMidiNote)
            assertEquals(1.0f, resolved.rate, 0.0001f)
        }
    }

    @Test
    fun `key one semitone above an anchor resolves to that anchor`() {
        val resolved = PianoSampleAnchors.resolve(22) // one above A0 (21), two below C1 (24)
        assertEquals(21, resolved.anchorMidiNote)
        assertEquals(2.0.pow(1.0 / 12.0).toFloat(), resolved.rate, 0.0001f)
    }

    @Test
    fun `key one semitone below an anchor resolves to that anchor`() {
        val resolved = PianoSampleAnchors.resolve(23) // two above A0 (21), one below C1 (24)
        assertEquals(24, resolved.anchorMidiNote)
        assertEquals(2.0.pow(-1.0 / 12.0).toFloat(), resolved.rate, 0.0001f)
    }

    @Test
    fun `boundary keys A0 and C8 resolve to themselves`() {
        assertEquals(21, PianoSampleAnchors.resolve(21).anchorMidiNote)
        assertEquals(108, PianoSampleAnchors.resolve(108).anchorMidiNote)
    }

    @Test
    fun `every key on the keyboard resolves within one semitone of an anchor`() {
        val maxRate = 2.0.pow(1.0 / 12.0).toFloat()
        val minRate = 2.0.pow(-1.0 / 12.0).toFloat()
        for (key in PianoKeyboardLayout.keys) {
            val rate = PianoSampleAnchors.resolve(key.midiNote).rate
            assertTrue(
                "rate $rate for midiNote ${key.midiNote} outside [$minRate, $maxRate]",
                rate in minRate..maxRate
            )
        }
    }

    @Test
    fun `anchor table is well formed`() {
        val anchors = PianoSampleAnchors.anchorMidiNotes
        assertEquals(30, anchors.size)
        assertEquals(21, anchors.first())
        assertEquals(108, anchors.last())
        assertEquals(anchors.sorted(), anchors)
        for (i in 1 until anchors.size) {
            assertEquals(3, anchors[i] - anchors[i - 1])
        }
    }
}
