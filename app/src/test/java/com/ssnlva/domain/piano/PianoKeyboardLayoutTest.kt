package com.ssnlva.domain.piano

import org.junit.Assert.assertEquals
import org.junit.Test

class PianoKeyboardLayoutTest {

    @Test
    fun `keyboard has eighty-eight keys`() {
        assertEquals(88, PianoKeyboardLayout.keys.size)
    }

    @Test
    fun `keyboard has fifty-two white and thirty-six black keys`() {
        val whiteCount = PianoKeyboardLayout.keys.count { !it.isBlack }
        val blackCount = PianoKeyboardLayout.keys.count { it.isBlack }
        assertEquals(52, whiteCount)
        assertEquals(36, blackCount)
        assertEquals(PianoKeyboardLayout.keys.size, whiteCount + blackCount)
    }

    @Test
    fun `black keys follow the real piano pattern within an octave`() {
        val isBlackPattern = PianoKeyboardLayout.keys.filter { it.octave == 4 }.map { it.isBlack }
        val expected = listOf(
            false, true, false, true, false, // C C# D D# E
            false, true, false, true, false, true, false, // F F# G G# A A# B
        )
        assertEquals(expected, isBlackPattern)
    }

    @Test
    fun `note names follow the real piano pattern within an octave`() {
        val names = PianoKeyboardLayout.keys.filter { it.octave == 4 }.map { it.name }
        val expected = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        assertEquals(expected, names)
    }

    @Test
    fun `keyboard starts at A0`() {
        val expected = listOf(
            PianoKey(name = "A", octave = 0, isBlack = false),
            PianoKey(name = "A#", octave = 0, isBlack = true),
            PianoKey(name = "B", octave = 0, isBlack = false),
        )
        assertEquals(expected, PianoKeyboardLayout.keys.take(3))
    }

    @Test
    fun `keyboard ends at C8`() {
        val expected = listOf(
            PianoKey(name = "B", octave = 7, isBlack = false),
            PianoKey(name = "C", octave = 8, isBlack = false),
        )
        assertEquals(expected, PianoKeyboardLayout.keys.takeLast(2))
    }

    @Test
    fun `octave number increments after B into the next C`() {
        val b3Index = PianoKeyboardLayout.keys.indexOfFirst { it.name == "B" && it.octave == 3 }
        val nextKey = PianoKeyboardLayout.keys[b3Index + 1]
        assertEquals(PianoKey(name = "C", octave = 4, isBlack = false), nextKey)
    }

    @Test
    fun `center key index resolves to middle C`() {
        assertEquals(
            PianoKey(name = "C", octave = 4, isBlack = false),
            PianoKeyboardLayout.keys[PianoKeyboardLayout.centerKeyIndex]
        )
    }

    @Test
    fun `display name combines letter name and octave`() {
        assertEquals("C4", PianoKey(name = "C", octave = 4, isBlack = false).displayName)
        assertEquals("A#0", PianoKey(name = "A#", octave = 0, isBlack = true).displayName)
    }

    @Test
    fun `no two keys share the same name and octave`() {
        val identities = PianoKeyboardLayout.keys.map { it.name to it.octave }
        assertEquals(identities.distinct().size, identities.size)
    }
}
