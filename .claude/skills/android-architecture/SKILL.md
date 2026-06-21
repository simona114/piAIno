---
name: android-architecture
description: Use this skill proactively before creating a new screen or feature, or deciding where code should live.
---

# Android Architecture

## Core Philosophy

- Follow Clean Architecture principles.
- Keep the architecture as simple as possible.
- Introduce additional layers only when they provide clear value.
- Avoid unnecessary abstractions and over-engineering.
- Prefer maintainable and readable code over clever solutions.

## Architecture

Layers:

### Presentation

Contains:
- Compose screens
- Reusable UI components
- ViewModels
- UiState
- Navigation logic

### Domain

Contains:
- Business logic
- Use cases
- Domain models

Rules:
- The domain layer must not depend on the presentation layer.
- Business logic should not live inside composables.
- ViewModels coordinate UI and domain logic.

## Navigation

- Use Compose Navigation with type-safe navigation.
- Navigation logic belongs in the presentation layer.
- Keep navigation definitions centralized and easy to discover.

## Preferences

Use SharedPreferences for persistent user preferences.

Examples:
- Theme
- Keyboard settings
- Note label preferences
- Sound settings
- Last selected octave

## Key Libraries

| Concern       | Library                        |
| ------------- | ------------------------------ |
| UI            | Jetpack Compose                |
| Navigation    | Compose Navigation (type-safe) |
| Preferences   | SharedPreferences              |
| Async         | Coroutines + Flow              |
| Image Loading | Coil                           |
| Testing       | JUnit                          |
| UI Testing    | Compose UI Testing             |

## Testing

- Write unit tests for business logic when appropriate.
- Use Compose UI Testing for UI verification.
- Focus on testing important behavior rather than implementation details.
