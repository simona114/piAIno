package com.ssnlva.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.ssnlva.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

// Headroom for a full ten-finger chord plus ringing decay tails from a previous chord.
private const val MaxStreams = 12

// Prevents notes from ending too abruptly when released.
private const val FadeOutSteps = 50
private const val FadeOutDelayMs = 10L

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
 * playback rate, for any of the 88 keys. Each [playNote] call plays an independent voice -
 * SoundPool's stream pool gives polyphony for free, so rapid/overlapping presses don't cut
 * off prior notes (until [MaxStreams] is exceeded). Tracks each note's active stream so the
 * sustain mechanism can later stop it on [releaseNote], or so a retrigger can stop a still-
 * ringing prior voice before starting the new one.
 */
class SoundPoolPianoPlayer(context: Context) : PianoSoundPlayer {

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

    // Sustain defaults on: matches a real piano's sustain pedal being the "hold the ring" case
    // a player reaches for, rather than the cut-on-release case being the default.
    private var sustainEnabled = true

    // midiNote -> currently playing SoundPool streamId. Each of the 88 keys maps to a unique
    // MIDI note (21-108), so keying by midiNote is collision-free across simultaneous presses.
    private val activeStreams = mutableMapOf<Int, Int>()

    // This class is an app-lifetime singleton (see Koin module), so a scope scoped to its own
    // lifetime - rather than a caller-supplied one - is appropriate. Main.immediate keeps the
    // fade-out's setVolume() calls off any background thread (SoundPool is not documented as
    // thread-safe) while still never blocking the caller of playNote/releaseNote.
    private val playerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // midiNote -> in-flight fade-out job, so a retrigger or a repeated release can cancel a
    // fade before its eventual stop() lands on a stream that no longer belongs to that note.
    private val fadeOutJobs = mutableMapOf<Int, Job>()

    // midiNote -> streamId currently being faded. Separate from activeStreams (which only
    // holds streams a future playNote/releaseNote should still act on) so a cancelled fade
    // knows which stream to stop immediately instead of leaving it ringing mid-fade.
    private val fadingStreams = mutableMapOf<Int, Int>()

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

    override fun setSustainEnabled(enabled: Boolean) {
        sustainEnabled = enabled
    }

    /** No-ops if [midiNote]'s anchor sample hasn't finished its (fast, startup-time-only)
     *  async load yet. */
    override fun playNote(midiNote: Int) {
        val resolved = PianoSampleAnchors.resolve(midiNote)
        val soundId = loadedSoundIds[resolved.anchorMidiNote] ?: return
        stopFadingStream(midiNote) // a fresh attack cuts cleanly, not blended with a fade tail
        activeStreams.remove(midiNote)?.let { soundPool.stop(it) } // retrigger cuts the prior ring
        val streamId = soundPool.play(soundId, 1f, 1f, 1, 0, resolved.rate)
        if (streamId != 0) activeStreams[midiNote] = streamId // 0 = SoundPool play failure, not a real stream
    }

    override fun releaseNote(midiNote: Int) {
        if (sustainEnabled) return
        stopFadingStream(midiNote) // a repeated release replaces, rather than stacks on, the fade
        val streamId = activeStreams.remove(midiNote) ?: return
        fadingStreams[midiNote] = streamId
        fadeOutJobs[midiNote] = playerScope.launch {
            for (step in 1..FadeOutSteps) {
                // Squared (not linear) taper: ear-perceived loudness tracks amplitude^2 more
                // closely than amplitude itself, and a real piano's string decay is itself
                // exponential-ish. A linear amplitude ramp over a full second would sound like
                // it loses most of its loudness in the first fraction of the fade, then lingers
                // near-silent for a long, draggy tail - audible at 1s even though it was too
                // brief to notice at the old 60ms duration. Squaring keeps the perceived loudness
                // drop closer to even across the second.
                val linear = 1f - step.toFloat() / FadeOutSteps
                val volume = linear * linear
                soundPool.setVolume(streamId, volume, volume)
                delay(FadeOutDelayMs.milliseconds)
            }
            soundPool.stop(streamId)
            fadeOutJobs.remove(midiNote)
            fadingStreams.remove(midiNote)
        }
    }

    /** Cancels [midiNote]'s in-flight fade-out job, if any, and immediately stops the stream
     *  it was fading - cancelling the job alone would abandon that stream ringing at whatever
     *  partial volume the fade had reached instead of cutting it, leaking a stream slot until
     *  the sample naturally finishes. */
    private fun stopFadingStream(midiNote: Int) {
        val fadingStreamId = fadingStreams.remove(midiNote) ?: return
        fadeOutJobs.remove(midiNote)?.cancel()
        soundPool.stop(fadingStreamId)
    }

    /** Releases all native SoundPool resources. Call exactly once when no longer needed. */
    fun release() {
        playerScope.cancel()
        soundPool.release()
    }
}
