package com.ssnlva.domain.piano

import org.junit.Assert.assertEquals
import org.junit.Test

class PianoOctaveLayoutTest {

    @Test
    fun `one octave has twelve keys`() {
        assertEquals(12, PianoOctaveLayout.keys.size)
    }

    @Test
    fun `one octave has seven white and five black keys`() {
        val whiteCount = PianoOctaveLayout.keys.count { !it.isBlack }
        val blackCount = PianoOctaveLayout.keys.count { it.isBlack }
        assertEquals(7, whiteCount)
        assertEquals(5, blackCount)
    }

    @Test
    fun `black keys follow the real piano pattern`() {
        val isBlackPattern = PianoOctaveLayout.keys.map { it.isBlack }
        val expected = listOf(
            false, true, false, true, false, // C C# D D# E
            false, true, false, true, false, true, false, // F F# G G# A A# B
        )
        assertEquals(expected, isBlackPattern)
    }
}
