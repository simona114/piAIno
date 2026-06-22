package com.ssnlva.ui.settings

import com.ssnlva.domain.piano.Notation
import com.ssnlva.domain.piano.PianoPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsViewModelTest {

    private class FakePianoPreferencesRepository(
        initial: Boolean,
        initialNotation: Notation = Notation.LETTER
    ) : PianoPreferencesRepository {
        private var enabled = initial
        private var sustainEnabled = true
        private var notation = initialNotation
        override fun isShowNoteNamesEnabled(): Boolean = enabled
        override fun setShowNoteNamesEnabled(enabled: Boolean) {
            this.enabled = enabled
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
    fun `defaults to the repository's current value`() {
        val viewModel = SettingsViewModel(FakePianoPreferencesRepository(initial = false))
        assertFalse(viewModel.showNoteNames)
    }

    @Test
    fun `toggling updates both exposed state and the repository`() {
        val repository = FakePianoPreferencesRepository(initial = true)
        val viewModel = SettingsViewModel(repository)

        viewModel.updateShowNoteNames(false)

        assertFalse(viewModel.showNoteNames)
        assertFalse(repository.isShowNoteNamesEnabled())
    }

    @Test
    fun `defaults to true when the repository starts enabled`() {
        val viewModel = SettingsViewModel(FakePianoPreferencesRepository(initial = true))
        assertTrue(viewModel.showNoteNames)
    }

    @Test
    fun `notation defaults to the repository's current value`() {
        val viewModel = SettingsViewModel(
            FakePianoPreferencesRepository(initial = true, initialNotation = Notation.SOLFEGE)
        )
        assertEquals(Notation.SOLFEGE, viewModel.notation)
    }

    @Test
    fun `updateNotation updates both exposed state and the repository`() {
        val repository = FakePianoPreferencesRepository(initial = true, initialNotation = Notation.LETTER)
        val viewModel = SettingsViewModel(repository)

        viewModel.updateNotation(Notation.SOLFEGE)

        assertEquals(Notation.SOLFEGE, viewModel.notation)
        assertEquals(Notation.SOLFEGE, repository.getNotation())
    }
}
