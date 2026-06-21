---
name: android-navigation
description: |
  Use this skill proactively before adding a new screen or changing navigation flow — read it before writing any NavHost, route, or screen-wiring code.
---

# Android Navigation

## Principles

* Keep navigation simple.
* All destinations are registered in a central `NavHost`.
* Use descriptive route names.
* Only introduce navigation arguments when needed.

---

## NavHost

All screens should be added to the application's `NavHost`.

Example:

```kotlin
NavHost(
    navController = navController,
    startDestination = PianoRoute
) {
    composable(PianoRoute) {
        PianoScreen()
    }

    composable(SettingsRoute) {
        SettingsScreen()
    }
}
```

---

## Naming Conventions

| Thing     | Convention          |
| --------- | ------------------- |
| Route     | `<Screen>Route`     |
| Screen    | `<Screen>Screen`    |
| ViewModel | `<Screen>ViewModel` |

Examples:

* SplashRoute
* PianoRoute
* SettingsRoute

---

## Checklist: Adding a New Screen

* [ ] Create the screen composable
* [ ] Create the ViewModel if needed
* [ ] Define a route
* [ ] Add the destination to the NavHost
* [ ] Add navigation actions from existing screens if required
