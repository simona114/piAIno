package com.ssnlva.ui.settings

import com.ssnlva.domain.piano.PianoPreferencesRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsViewModelTest {

    private class FakePianoPreferencesRepository(initial: Boolean) : PianoPreferencesRepository {
        private var enabled = initial
        override fun isShowNoteNamesEnabled(): Boolean = enabled
        override fun setShowNoteNamesEnabled(enabled: Boolean) {
            this.enabled = enabled
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
}
