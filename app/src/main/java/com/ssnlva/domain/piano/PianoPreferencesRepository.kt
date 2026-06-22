package com.ssnlva.domain.piano

import android.content.Context

interface PianoPreferencesRepository {
    fun isShowNoteNamesEnabled(): Boolean
    fun setShowNoteNamesEnabled(enabled: Boolean)
    fun isSustainEnabled(): Boolean
    fun setSustainEnabled(enabled: Boolean)
    fun getNotation(): Notation
    fun setNotation(notation: Notation)
}

class SharedPreferencesPianoRepository(context: Context) : PianoPreferencesRepository {
    private val prefs = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)

    override fun isShowNoteNamesEnabled(): Boolean =
        prefs.getBoolean(KeyShowNoteNames, true)

    override fun setShowNoteNamesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KeyShowNoteNames, enabled).apply()
    }

    override fun isSustainEnabled(): Boolean =
        prefs.getBoolean(KeySustainEnabled, true)

    override fun setSustainEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KeySustainEnabled, enabled).apply()
    }

    override fun getNotation(): Notation {
        val storedName = prefs.getString(KeyNotation, null) ?: return Notation.LETTER
        return runCatching { Notation.valueOf(storedName) }.getOrDefault(Notation.LETTER)
    }

    override fun setNotation(notation: Notation) {
        prefs.edit().putString(KeyNotation, notation.name).apply()
    }

    private companion object {
        const val PrefsName = "piano_preferences"
        const val KeyShowNoteNames = "show_note_names"
        const val KeySustainEnabled = "sustain_enabled"
        const val KeyNotation = "notation"
    }
}
