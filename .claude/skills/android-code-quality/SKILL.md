---
name: android-code-quality
description: |
  Use this skill proactively whenever writing or changing Kotlin/Android code, before the edit not after — naming, null-safety, immutability, error handling, and general code hygiene. Complements android-architecture, android-navigation, and android-compose-ui, which cover structural concerns.
---

# Android Code Quality

## Core Principle

Code should be readable and obvious before it is clever. Prefer the boring, idiomatic Kotlin solution over a novel one.

---

## Naming

* Classes/objects: `PascalCase`. Functions/properties: `camelCase`. Constants: `UPPER_SNAKE_CASE`.
* Name things for what they represent, not how they're implemented (`sustainEnabled`, not `sustainFlag`).
* Avoid abbreviations except well-known domain terms (`octave`, `note`, not `oct`, `nt`).
* Boolean names read as a yes/no question: `isPlaying`, `hasSustain`, `canScroll`.

---

## Null Safety & Immutability

* Avoid nullable types unless absence is a real, meaningful state. Don't use `null` as a stand-in for "not loaded yet" when a sealed state or default value works.
* Never use `!!`. Use safe calls, `requireNotNull`, or restructure so the null case can't occur.
* Prefer `val` over `var`. A `var` should be justified by genuine mutability (e.g., an in-progress accumulator), not convenience.
* Prefer immutable collections (`List`, `Map`) at API boundaries; keep mutable collections (`MutableList`) internal to the function/class that builds them.

---

## Functions & Classes

* Keep functions small and single-purpose. If you need a comment to separate "sections" of a function, split it.
* Prefer top-level or extension functions over utility classes with static-style methods.
* Default parameter values over overloads.
* Data classes for plain data; avoid adding behavior beyond simple derived properties to them.

---

## Error Handling

* Don't catch exceptions you can't meaningfully handle — let them propagate.
* Don't use exceptions for expected control flow (e.g., end of input, missing optional value) — model that with a return type instead.
* Validate only at real boundaries (user input, file/audio resource loading). Don't add defensive checks for states that are already guaranteed by the type system or caller.

---

## Resources & Constants

* No hardcoded user-facing strings in code — use string resources, even for a single-locale app, so display text stays in one place.
* No magic numbers for domain values (e.g., semitone counts, octave ranges) — name them as constants where they appear more than once.

---

## Comments & Documentation

* Default to no comments. Code should read clearly from names and structure.
* Write a comment only to explain a non-obvious *why* — a constraint, a workaround, an invariant that isn't visible from the code itself.
* Don't leave commented-out code or TODOs without an owner/reason.

---

## Coroutines & Async

* Scope coroutines to the lifecycle that owns them (`viewModelScope`, not `GlobalScope`).
* Prefer `Flow` for streams of values over manual callback patterns.
* Keep suspend functions free of UI/Android framework references so they stay testable.

---

## General Hygiene

* Remove dead code instead of disabling it — don't keep unused functions, parameters, or imports "just in case."
* Keep one public type's primary logic in one file; avoid splitting a single class across files.
* Match existing formatting in the file you're editing over introducing a new style.
