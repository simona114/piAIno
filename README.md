# piAIno

A virtual piano for Android. Play all 88 keys, scroll across octaves, and switch between letter and solfège notation - with realistic sampled sound and a sustain mode that mirrors a real piano pedal.


---

## Features

- **88-key piano keyboard** - full range from A0 to C8, horizontally scrollable
- **Realistic sound** - 30 recorded grand piano samples, pitch-shifted to cover every key
- **Polyphony** - hold or rapid-press multiple keys without notes cutting each other off
- **Sustain mode** - notes ring after release, just like a sustain pedal
- **Note labels** - toggle note names on the white keys, in letter (C, D#) or solfège (Do, Re#) notation
- **Low-latency audio** - routed through Android's fast audio path for immediate response to touch

---

## Screenshots/Videos

### Splash screen
![Splash screen](documentation/screenshots/demo-splash.jpg)

### Piano
![Piano screen](documentation/screenshots/demo-piano.jpg)

### Settings
![Settings screen](documentation/screenshots/demo-settings.jpg)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM |
| Navigation | Compose Navigation (typed routes) |
| Audio | SoundPool (USAGE_GAME, fast mixer path) |
| Preferences | SharedPreferences |

---

## Architecture

The app is split into three logical modules (packages within a single Gradle module):

**Presentation** - Compose screens (Splash, Piano, Settings), ViewModels, and the custom Canvas-drawn keyboard. The keyboard is drawn as a single Canvas rather than per-key composables to support smooth scrolling, black-key overlap, and simultaneous multi-touch at scale.

**Piano Domain** - Pure Kotlin, no Android imports. Owns the 88-key layout, note naming in both notation systems, pitch-shift rate math, and user preferences. Both the UI and audio layers depend on this; neither depends on the other.

**Audio Playback** - Loads the 30 anchor samples asynchronously on startup (center octave first, so the visible viewport is playable immediately), resolves any key press to its nearest anchor, and pitch-shifts via SoundPool playback rate. Manages polyphonic voice lifecycle, sustain fade-out, and thread safety between the binder-thread load callbacks and main-thread playback calls.

---

## AI Customizations & Automations

The project was developed using [Claude Code](https://claude.ai/code) with several customizations that shaped the workflow.

### Specialist sub-agents

Two domain-specific agents were configured - `android-ui-engineer` and `android-audio-engineer` - each scoped to its own module. They could be dispatched for focused code review or implementation work, and run in parallel when the tasks were independent.

![Specialist split planning](documentation/screenshots/ai-planning.png)

![Parallel agents running](documentation/screenshots/ai-parallel-agents.png)

### Skills

Reusable skills were set up for recurring workflows: splitting uncommitted changes into atomic commits, verifying UI against mockups, and delegating implementation tasks. A skill is invoked by name and runs a predefined sequence of steps.

![Skill invocation](documentation/screenshots/ai-skill.png)

### Task planning

For multi-step work, Claude Code broke the plan into tracked subtasks with explicit blocking relationships - a task depending on another could not start until its dependency was resolved.

![Task planning with subtasks](documentation/screenshots/ai-tasks.png)

### Prompt logging hook

A hook was configured to log every prompt sent during a session to a local JSON file. This produced a record of the full interaction history that could be reviewed after the session.

### Emulator verification

For visual work (app icon, splash screen), the agent took a screenshot from a running emulator and compared it against the mockup automatically, reporting pass/fail per element.

<!-- TODO: screenshot - emulator verification results -->

---

## Sound Samples

Piano sounds are from the **Salamander Grand Piano** by Alexander Holm, licensed under [CC BY 3.0](https://creativecommons.org/licenses/by/3.0/).

- Original recordings: [archive.org/details/SalamanderGrandPianoV3](https://archive.org/details/SalamanderGrandPianoV3)
- Packaged via: [darosh/samples-piano-mp3](https://github.com/darosh/samples-piano-mp3)

30 of the 88 keys are directly recorded; the remaining 58 are pitch-shifted from the nearest recorded sample, never more than ~1.5 semitones away.

---
