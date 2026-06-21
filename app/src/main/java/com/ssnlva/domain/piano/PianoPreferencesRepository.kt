package com.ssnlva.domain.piano

import android.content.Context

interface PianoPreferencesRepository {
    fun isShowNoteNamesEnabled(): Boolean
    fun setShowNoteNamesEnabled(enabled: Boolean)
}

class SharedPreferencesPianoRepository(context: Context) : PianoPreferencesRepository {
    private val prefs = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)

    override fun isShowNoteNamesEnabled(): Boolean =
        prefs.getBoolean(KeyShowNoteNames, true)

    override fun setShowNoteNamesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KeyShowNoteNames, enabled).apply()
    }

    private companion object {
        const val PrefsName = "piano_preferences"
        const val KeyShowNoteNames = "show_note_names"
    }
}
