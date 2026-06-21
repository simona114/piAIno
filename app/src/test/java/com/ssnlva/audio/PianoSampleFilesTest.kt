package com.ssnlva.audio

import com.ssnlva.domain.piano.PianoKeyboardLayout
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the actual sample files bundled in `app/src/main/res/raw/` match what
 * [PianoSampleAnchors] expects to be loadable - exactly one file per anchor note, named per
 * the project's resource-naming convention (lowercase letter, `s` for sharp, octave digit -
 * e.g. `ds4` for D#4). The Kotlin compiler alone can't catch a deleted or mis-renamed sample
 * file if an `R.raw` reference happens to still resolve to *some* other resource, so this
 * checks the files on disk directly rather than only the generated `R` class.
 */
class PianoSampleFilesTest {

    private val rawDir = File("src/main/res/raw")

    @Test
    fun `res raw directory exists`() {
        assertTrue("expected ${rawDir.absolutePath} to exist", rawDir.isDirectory)
    }

    @Test
    fun `there is exactly one sample file for every anchor note, named per convention`() {
        val expectedNames = PianoSampleAnchors.anchorMidiNotes.map(::expectedResourceName).toSet()
        val actualNames = rawDir.listFiles { file -> file.isFile }
            .orEmpty()
            .map { it.nameWithoutExtension }
            .toSet()
        assertEquals(expectedNames, actualNames)
    }

    @Test
    fun `no sample file is empty or obviously truncated`() {
        val files = rawDir.listFiles { file -> file.isFile }.orEmpty()
        assertTrue("expected to find sample files in $rawDir", files.isNotEmpty())
        for (file in files) {
            assertTrue(
                "${file.name} is suspiciously small (${file.length()} bytes)",
                file.length() > 1024
            )
        }
    }

    private fun expectedResourceName(midiNote: Int): String {
        val key = PianoKeyboardLayout.keys.first { it.midiNote == midiNote }
        return key.name.replace("#", "s").lowercase() + key.octave
    }
}
