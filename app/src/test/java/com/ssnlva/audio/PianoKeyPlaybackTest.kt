package com.ssnlva.audio

import com.ssnlva.domain.piano.PianoKey
import com.ssnlva.domain.piano.PianoKeyboardLayout
import kotlin.math.pow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies that pressing any real key on the keyboard - white or black - resolves to both a
 * sample that is actually wired up for loading ([AnchorRawResources]) and a pitch within the
 * one-semitone bound the anchor spacing guarantees. Covers white and black keys separately so
 * neither group can silently fall through a gap (e.g. an anchor list that forgot a sharp note).
 */
class PianoKeyPlaybackTest {

    private val whiteKeys = PianoKeyboardLayout.keys.filter { !it.isBlack }
    private val blackKeys = PianoKeyboardLayout.keys.filter { it.isBlack }

    @Test
    fun `keyboard has the expected white and black key counts`() {
        assertEquals(52, whiteKeys.size)
        assertEquals(36, blackKeys.size)
    }

    @Test
    fun `every one of the 88 keys resolves to a playable sound`() {
        val allKeys = PianoKeyboardLayout.keys
        assertEquals(88, allKeys.size)
        assertAllHaveRegisteredSamples(allKeys)
        assertAllWithinOneSemitone(allKeys)
    }

    @Test
    fun `every white key resolves to a sample that is actually registered for loading`() {
        assertAllHaveRegisteredSamples(whiteKeys)
    }

    @Test
    fun `every black key resolves to a sample that is actually registered for loading`() {
        assertAllHaveRegisteredSamples(blackKeys)
    }

    @Test
    fun `every white key pitches within one semitone of its sample`() {
        assertAllWithinOneSemitone(whiteKeys)
    }

    @Test
    fun `every black key pitches within one semitone of its sample`() {
        assertAllWithinOneSemitone(blackKeys)
    }

    @Test
    fun `white key middle C is an exact anchor at rate one`() {
        val middleC = PianoKeyboardLayout.keys[PianoKeyboardLayout.centerKeyIndex]
        val resolved = PianoSampleAnchors.resolve(middleC.midiNote)
        assertEquals(60, resolved.anchorMidiNote)
        assertEquals(1.0f, resolved.rate, 0.0001f)
        assertTrue(AnchorRawResources.containsKey(resolved.anchorMidiNote))
    }

    @Test
    fun `black key D sharp 4 is an exact anchor at rate one`() {
        val dSharp4 = keyNamed("D#", 4)
        val resolved = PianoSampleAnchors.resolve(dSharp4.midiNote)
        assertEquals(63, resolved.anchorMidiNote)
        assertEquals(1.0f, resolved.rate, 0.0001f)
        assertTrue(AnchorRawResources.containsKey(resolved.anchorMidiNote))
    }

    @Test
    fun `black key C sharp 4 pitches up one semitone from middle C's sample`() {
        val cSharp4 = keyNamed("C#", 4)
        val resolved = PianoSampleAnchors.resolve(cSharp4.midiNote)
        assertEquals(60, resolved.anchorMidiNote) // C4
        assertEquals(2.0.pow(1.0 / 12.0).toFloat(), resolved.rate, 0.0001f)
    }

    @Test
    fun `white key B0 pitches down one semitone from C1's sample`() {
        val b0 = keyNamed("B", 0)
        val resolved = PianoSampleAnchors.resolve(b0.midiNote)
        assertEquals(24, resolved.anchorMidiNote) // C1
        assertEquals(2.0.pow(-1.0 / 12.0).toFloat(), resolved.rate, 0.0001f)
    }

    private fun keyNamed(name: String, octave: Int): PianoKey =
        PianoKeyboardLayout.keys.first { it.name == name && it.octave == octave }

    private fun assertAllHaveRegisteredSamples(keys: List<PianoKey>) {
        for (key in keys) {
            val anchor = PianoSampleAnchors.resolve(key.midiNote).anchorMidiNote
            assertTrue(
                "no raw resource registered for anchor $anchor (key ${key.name}${key.octave})",
                AnchorRawResources.containsKey(anchor)
            )
        }
    }

    private fun assertAllWithinOneSemitone(keys: List<PianoKey>) {
        val maxRate = 2.0.pow(1.0 / 12.0).toFloat()
        val minRate = 2.0.pow(-1.0 / 12.0).toFloat()
        for (key in keys) {
            val rate = PianoSampleAnchors.resolve(key.midiNote).rate
            assertTrue(
                "rate $rate for ${key.name}${key.octave} (midi ${key.midiNote}) outside [$minRate, $maxRate]",
                rate in minRate..maxRate
            )
        }
    }
}
