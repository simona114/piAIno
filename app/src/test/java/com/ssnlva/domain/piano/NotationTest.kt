package com.ssnlva.domain.piano

import org.junit.Assert.assertEquals
import org.junit.Test

class NotationTest {

    private val chromaticNames = listOf(
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B",
    )

    @Test
    fun `letter notation returns the pitch class name unchanged`() {
        chromaticNames.forEach { name ->
            assertEquals(name, Notation.LETTER.labelFor(name))
        }
    }

    @Test
    fun `solfege notation maps natural pitch classes to their syllable`() {
        assertEquals("Do", Notation.SOLFEGE.labelFor("C"))
        assertEquals("Re", Notation.SOLFEGE.labelFor("D"))
        assertEquals("Mi", Notation.SOLFEGE.labelFor("E"))
        assertEquals("Fa", Notation.SOLFEGE.labelFor("F"))
        assertEquals("Sol", Notation.SOLFEGE.labelFor("G"))
        assertEquals("La", Notation.SOLFEGE.labelFor("A"))
        assertEquals("Si", Notation.SOLFEGE.labelFor("B"))
    }

    @Test
    fun `solfege notation appends a sharp suffix for sharp pitch classes`() {
        assertEquals("Do#", Notation.SOLFEGE.labelFor("C#"))
        assertEquals("Re#", Notation.SOLFEGE.labelFor("D#"))
        assertEquals("Fa#", Notation.SOLFEGE.labelFor("F#"))
        assertEquals("Sol#", Notation.SOLFEGE.labelFor("G#"))
        assertEquals("La#", Notation.SOLFEGE.labelFor("A#"))
    }
}
