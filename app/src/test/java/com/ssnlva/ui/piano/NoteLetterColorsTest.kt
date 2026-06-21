package com.ssnlva.ui.piano

import com.ssnlva.domain.piano.PianoKeyboardLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteLetterColorsTest {

    @Test
    fun `every white key letter has a registered color`() {
        val whiteKeyLetters = PianoKeyboardLayout.keys.filter { !it.isBlack }.map { it.name }.distinct()
        for (letter in whiteKeyLetters) {
            assertTrue("no color registered for letter $letter", NoteLetterColors.containsKey(letter))
        }
    }

    @Test
    fun `every registered color is distinct`() {
        assertEquals(NoteLetterColors.size, NoteLetterColors.values.toSet().size)
    }
}
