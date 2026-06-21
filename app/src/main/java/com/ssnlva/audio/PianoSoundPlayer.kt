package com.ssnlva.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.ssnlva.R

// Headroom for a full ten-finger chord plus ringing decay tails from a previous chord.
private const val MaxStreams = 12

// internal (not private) so tests can verify every sampled anchor has a backing resource.
internal val AnchorRawResources: Map<Int, Int> = mapOf(
    21 to R.raw.a0, 24 to R.raw.c1, 27 to R.raw.ds1, 30 to R.raw.fs1,
    33 to R.raw.a1, 36 to R.raw.c2, 39 to R.raw.ds2, 42 to R.raw.fs2,
    45 to R.raw.a2, 48 to R.raw.c3, 51 to R.raw.ds3, 54 to R.raw.fs3,
    57 to R.raw.a3, 60 to R.raw.c4, 63 to R.raw.ds4, 66 to R.raw.fs4,
    69 to R.raw.a4, 72 to R.raw.c5, 75 to R.raw.ds5, 78 to R.raw.fs5,
    81 to R.raw.a5, 84 to R.raw.c6, 87 to R.raw.ds6, 90 to R.raw.fs6,
    93 to R.raw.a6, 96 to R.raw.c7, 99 to R.raw.ds7, 102 to R.raw.fs7,
    105 to R.raw.a7, 108 to R.raw.c8,
)

/**
 * Loads the 30 sampled piano anchor notes and plays the nearest one, pitch-shifted via
 * playback rate, for any of the 88 keys. Each [playNote] call is an independent one-shot
 * voice - SoundPool's stream pool gives polyphony for free, so rapid/overlapping presses
 * don't cut off prior notes (until [MaxStreams] is exceeded).
 */
class PianoSoundPlayer(context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(MaxStreams)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .build()

    // anchor midiNote -> loaded SoundPool soundId, populated as each async load completes.
    private val loadedSoundIds = mutableMapOf<Int, Int>()

    init {
        val pendingByLoadId = mutableMapOf<Int, Int>() // SoundPool load id -> anchor midiNote
        soundPool.setOnLoadCompleteListener { _, soundId, status ->
            val anchorMidiNote = pendingByLoadId.remove(soundId)
            if (status == 0 && anchorMidiNote != null) {
                loadedSoundIds[anchorMidiNote] = soundId
            }
        }
        for ((anchorMidiNote, rawResId) in AnchorRawResources) {
            val loadId = soundPool.load(context, rawResId, 1)
            pendingByLoadId[loadId] = anchorMidiNote
        }
    }

    /** No-ops if [midiNote]'s anchor sample hasn't finished its (fast, startup-time-only)
     *  async load yet. */
    fun playNote(midiNote: Int) {
        val resolved = PianoSampleAnchors.resolve(midiNote)
        val soundId = loadedSoundIds[resolved.anchorMidiNote] ?: return
        soundPool.play(soundId, 1f, 1f, 1, 0, resolved.rate)
    }

    /** Releases all native SoundPool resources. Call exactly once when no longer needed. */
    fun release() {
        soundPool.release()
    }
}
