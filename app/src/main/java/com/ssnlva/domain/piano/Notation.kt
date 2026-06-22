package com.ssnlva.domain.piano

/** A naming system for pitch classes, selectable as a user preference. */
enum class Notation { LETTER, SOLFEGE }

/** Syllable for each natural pitch class under [Notation.SOLFEGE], per the app's specification. */
private val SolfegeSyllables: Map<String, String> = mapOf(
    "C" to "Do",
    "D" to "Re",
    "E" to "Mi",
    "F" to "Fa",
    "G" to "Sol",
    "A" to "La",
    "B" to "Si",
)

/** Display label for [pitchClassName] (a [PianoKey.name] value, e.g. "C", "C#") under this notation. */
fun Notation.labelFor(pitchClassName: String): String = when (this) {
    Notation.LETTER -> pitchClassName
    Notation.SOLFEGE -> {
        val natural = pitchClassName.removeSuffix("#")
        val syllable = requireNotNull(SolfegeSyllables[natural]) {
            "No solfège syllable for pitch class \"$pitchClassName\""
        }
        if (pitchClassName.endsWith("#")) "$syllable#" else syllable
    }
}
