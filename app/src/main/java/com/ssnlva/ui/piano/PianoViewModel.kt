package com.ssnlva.ui.piano

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ssnlva.audio.PianoSoundPlayer
import com.ssnlva.domain.piano.Notation
import com.ssnlva.domain.piano.PianoPreferencesRepository

class PianoViewModel(
    private val soundPlayer: PianoSoundPlayer,
    private val repository: PianoPreferencesRepository
) : ViewModel() {

    var sustainEnabled by mutableStateOf(repository.isSustainEnabled())
        private set

    var showNoteNames by mutableStateOf(repository.isShowNoteNamesEnabled())
        private set

    var notation by mutableStateOf(repository.getNotation())
        private set

    init {
        soundPlayer.setSustainEnabled(sustainEnabled)
    }

    fun onSustainToggle() {
        sustainEnabled = !sustainEnabled
        repository.setSustainEnabled(sustainEnabled)
        soundPlayer.setSustainEnabled(sustainEnabled)
    }

    fun onKeyPressed(midiNote: Int) = soundPlayer.playNote(midiNote)

    fun onKeyReleased(midiNote: Int) = soundPlayer.releaseNote(midiNote)

    fun refreshPreferences() {
        showNoteNames = repository.isShowNoteNamesEnabled()
        notation = repository.getNotation()
    }
}
