package com.ssnlva.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ssnlva.domain.piano.PianoPreferencesRepository

class SettingsViewModel(
    private val repository: PianoPreferencesRepository
) : ViewModel() {

    var showNoteNames by mutableStateOf(repository.isShowNoteNamesEnabled())
        private set

    fun updateShowNoteNames(enabled: Boolean) {
        showNoteNames = enabled
        repository.setShowNoteNamesEnabled(enabled)
    }
}
