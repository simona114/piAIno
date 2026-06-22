package com.ssnlva.ui.piano

import com.ssnlva.audio.PianoSoundPlayer
import com.ssnlva.domain.piano.Notation
import com.ssnlva.domain.piano.PianoPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PianoViewModelTest {

    private class FakePianoSoundPlayer : PianoSoundPlayer {
        val playedNotes = mutableListOf<Int>()
        val releasedNotes = mutableListOf<Int>()
        var lastSustainEnabled: Boolean? = null

        override fun playNote(midiNote: Int) {
            playedNotes += midiNote
        }

        override fun releaseNote(midiNote: Int) {
            releasedNotes += midiNote
        }

        override fun setSustainEnabled(enabled: Boolean) {
            lastSustainEnabled = enabled
        }
    }

    private class FakePianoPreferencesRepository(
        showNoteNames: Boolean = true,
        sustain: Boolean = true,
        notation: Notation = Notation.LETTER
    ) : PianoPreferencesRepository {
        private var showNoteNamesEnabled = showNoteNames
        private var sustainEnabled = sustain
        private var notation = notation

        override fun isShowNoteNamesEnabled(): Boolean = showNoteNamesEnabled
        override fun setShowNoteNamesEnabled(enabled: Boolean) {
            showNoteNamesEnabled = enabled
        }

        override fun isSustainEnabled(): Boolean = sustainEnabled
        override fun setSustainEnabled(enabled: Boolean) {
            sustainEnabled = enabled
        }

        override fun getNotation(): Notation = notation
        override fun setNotation(notation: Notation) {
            this.notation = notation
        }
    }

    @Test
    fun `constructing the view model pushes the repository's sustain value into the sound player`() {
        val soundPlayer = FakePianoSoundPlayer()
        PianoViewModel(soundPlayer, FakePianoPreferencesRepository(sustain = false))

        assertEquals(false, soundPlayer.lastSustainEnabled)
    }

    @Test
    fun `onSustainToggle flips state, persists it, and forwards it to the sound player`() {
        val soundPlayer = FakePianoSoundPlayer()
        val repository = FakePianoPreferencesRepository(sustain = true)
        val viewModel = PianoViewModel(soundPlayer, repository)

        viewModel.onSustainToggle()

        assertFalse(viewModel.sustainEnabled)
        assertFalse(repository.isSustainEnabled())
        assertEquals(false, soundPlayer.lastSustainEnabled)
    }

    @Test
    fun `onKeyPressed and onKeyReleased delegate straight through to the sound player`() {
        val soundPlayer = FakePianoSoundPlayer()
        val viewModel = PianoViewModel(soundPlayer, FakePianoPreferencesRepository())

        viewModel.onKeyPressed(60)
        viewModel.onKeyReleased(60)

        assertEquals(listOf(60), soundPlayer.playedNotes)
        assertEquals(listOf(60), soundPlayer.releasedNotes)
    }

    @Test
    fun `defaults sustainEnabled to true when the repository starts enabled`() {
        val viewModel = PianoViewModel(FakePianoSoundPlayer(), FakePianoPreferencesRepository(sustain = true))
        assertTrue(viewModel.sustainEnabled)
    }

    @Test
    fun `showNoteNames and notation default to the repository's current values at construction`() {
        val repository = FakePianoPreferencesRepository(showNoteNames = false, notation = Notation.SOLFEGE)
        val viewModel = PianoViewModel(FakePianoSoundPlayer(), repository)

        assertFalse(viewModel.showNoteNames)
        assertEquals(Notation.SOLFEGE, viewModel.notation)
    }

    @Test
    fun `refreshPreferences re-reads showNoteNames and notation after the repository changes`() {
        val repository = FakePianoPreferencesRepository(showNoteNames = true, notation = Notation.LETTER)
        val viewModel = PianoViewModel(FakePianoSoundPlayer(), repository)

        repository.setShowNoteNamesEnabled(false)
        repository.setNotation(Notation.SOLFEGE)
        viewModel.refreshPreferences()

        assertFalse(viewModel.showNoteNames)
        assertEquals(Notation.SOLFEGE, viewModel.notation)
    }
}
