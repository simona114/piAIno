# piAIno System Architecture

This document breaks the app down into technological modules based on `specifications.md`. A "module" here means a logical grouping of responsibility (e.g. a package within the existing single `:app` Gradle module) — not a separate Gradle module. The project stays single-module for now
## Modules

### Presentation

Renders the app's screens (Splash, Piano, Settings), owns all `ViewModel`/`UiState`, and hosts navigation between them. Contains the custom-drawn keyboard component — white/black key layout rendering, overlap, horizontal octave scrolling, label visibility — and translates user gestures into calls against Piano Domain and Audio Playback.

**Why separate:** It's the only layer allowed to touch Compose/Android UI APIs. The spec's visually exacting rendering requirements (key overlap/positioning, scrolling, label toggling, splash) are a pure rendering concern that shouldn't be entangled with note computation or sound production.

### Piano Domain

Pure-Kotlin, framework-free model of piano theory and app preferences: the 12-semitone chromatic sequence, octave math (centered on Middle C/C4), white/black key layout rules, letter ↔ solfège notation mapping, the sustain *rule* (what "extend playback" means, not how it's mechanically done), and the small set of persisted user preferences (sustain enabled, label visibility, active notation, last octave) via a repository-style interface backed by SharedPreferences.

**Why separate:** Both Presentation (to know what keys/labels to draw) and Audio Playback (to resolve a key to a pitch and know what sustain means) must consult the same source of truth. A neutral, dependency-free module lets both depend inward on it without depending on each other.

Preferences are folded in here rather than given their own module — persisting sustain/label/notation/octave is a handful of key-value reads/writes behind one small interface, with no queries, migrations, or multiple implementations. If persistence needs grow substantially later, this is the natural seam to split out.

### Audio Playback

Loads/holds piano sound samples, maps a resolved note (from Piano Domain) to playback, and manages overlapping/concurrent voices so rapid consecutive key presses don't cut off prior notes (polyphony). Implements the sustain *mechanism* (extending a voice's playback after key-up) as a real-time concern — timing, voice lifecycle, resource pooling.

**Why separate:** The spec singles this out twice — minimizing key-press-to-sound latency, and supporting rapid/overlapping presses without interrupting playback. This has its own technology (low-latency Android audio APIs, pooled playback resources, careful threading) and failure modes (glitches, dropped notes) unrelated to note-naming or layout math. It depends on Piano Domain; Piano Domain does not depend on it.

**Sample source:** 30 real piano note recordings (one every 3 semitones, A0-C8), pitch-shifted via `SoundPool` playback rate to cover the other 58 keys. Samples are from the "Salamander Grand Piano" by Alexander Holm, licensed under [CC BY 3.0](https://creativecommons.org/licenses/by/3.0/), original recordings at [archive.org/details/SalamanderGrandPianoV3](https://archive.org/details/SalamanderGrandPianoV3), downloaded via the [darosh/samples-piano-mp3](https://github.com/darosh/samples-piano-mp3) packaging (served from jsdelivr). See `app/src/main/assets/CREDITS.md` for the bundled attribution.

## Testing

Testing is a cross-cutting practice within each module, not a module of its own:

* Piano Domain: plain JUnit tests (chromatic math, layout generation, notation mapping) — pure Kotlin, cheap and high-value.
* Audio Playback: unit tests for isolable logic (voice allocation/polyphony bookkeeping); otherwise verified behaviorally, since it's inherently hardware/OS-dependent.
* Presentation: Compose UI tests for rendering/interaction (key press triggers correct action, scroll reveals correct octave, label toggle changes text).
