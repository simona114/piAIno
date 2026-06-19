# piAIno Specifications

## Vision

piAIno is an Android application that provides a realistic virtual piano experience, allowing users to play piano notes, navigate through multiple octaves, and customize how notes are displayed while interacting with a responsive and visually appealing piano keyboard.

## Requirements

### Users should be able to

* Play piano notes using a virtual piano keyboard
* Navigate through multiple piano octaves
* Hear realistic piano sounds when pressing keys
* Enable or disable sustain mode
* Show or hide note names on piano keys to assist beginner players
* Switch between letter notation (A-B-C) and solfège notation (Do-Re-Mi) to support different learning systems

### Functional Requirements

* Display a piano keyboard consisting of white and black keys
* Support horizontal scrolling between octaves
* Support rapid consecutive key presses without interrupting audio playback
* Extend note playback when sustain mode is enabled
* Display a splash screen during application startup

### Non-Functional Requirements

* The application should be developed using Kotlin and Jetpack Compose
* The application should follow MVVM architecture principles
* The application should remain responsive during all user interactions
* The application should minimize the delay between key presses and audio playback
* Code should be organized, maintainable, and well-documented

## Piano Domain Knowledge

### Keyboard Layout

- The keyboard should mimic the layout of a real piano
- White keys represent natural notes
- Black keys represent sharps and flats
- Black keys are visually positioned above and between the corresponding white keys
- Black keys should partially overlap the white keys, similar to a real piano
- The keyboard should be horizontally scrollable
### Initial Position

* The keyboard should initially be centered around Middle C (C4)
* Users can scroll to lower or higher octaves after startup

### Black Key Pattern

The black key layout repeats across all octaves:

* Two black keys: C#, D#
* No black key between E and F
* Three black keys: F#, G#, A#
* No black key between B and C

The note sequence is:

* C
* C#
* D
* D#
* E
* F
* F#
* G
* G#
* A
* A#
* B

### Octaves

* Each octave contains 12 semitones
* The note sequence repeats across octaves

### Note Naming Systems

Letter notation:

* C
* D
* E
* F
* G
* A
* B

Solfège notation:

* Do
* Re
* Mi
* Fa
* Sol
* La
* Si

### Sustain Mode

* Sustain mode extends note playback after a key is released
* It mimics the behavior of a sustain pedal on a real piano
