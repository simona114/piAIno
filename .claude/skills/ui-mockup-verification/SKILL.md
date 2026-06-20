---
name: ui-mockup-verification
description: |
 Use this skill as a verification step whenever implementing a visible Compose UI change (new screen, layout change, text/color/style change). Generates an HTML mockup of the proposed change, gets it approved, implements the real change, then screenshots the running app and compares it against the approved mockup before asking for final code approval.
---

# UI Mockup Verification

## Core Principle

A visible UI change gets validated twice, against two different things the user approves separately: the *intended look* (the mockup) before any code is written, and the *real result* (a device screenshot) plus the *code itself* after implementation. Don't skip either gate, and don't merge them — a screenshot matching the mockup does not imply the user has approved the diff.

---

## Workflow

1. **Build the mockup.** Write a small HTML/CSS page that visually approximates the proposed screen/change — phone-sized viewport, real theme colors from `app/src/main/java/com/ssnlva/ui/theme/Color.kt`/`Theme.kt` (M3 baseline defaults if unset: background `#FFFBFE`, text `#1C1B1F`), real copy. Don't aim for pixel-perfect fidelity — it's a wireframe for sign-off, not a design comp.

2. **Render it to a photo.** Use a headless browser already on this machine:
   ```
   & "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" --headless --disable-gpu --screenshot="<abs-path-to-mockup.png>" --window-size=412,915 "file:///<abs-path-to-mockup.html>"
   ```
   Fall back to Chrome (`C:\Program Files\Google\Chrome\Application\chrome.exe`) with the same flags if Edge isn't present. Some Chromium builds ignore `--screenshot=<path>` and always write `screenshot.png` to the current directory — if so, `cd` into the target folder first and rename the output.

3. **Approval gate #1 — the mockup.** Show the rendered PNG to the user (Read renders images inline). Do not write any app code until they explicitly approve it. If they request changes, edit the HTML, re-render, show again.

4. **Implement the real change** in the actual Compose source, once the mockup is approved.

5. **Build, install, launch, screenshot the real app.**
   ```
   .\gradlew.bat installDebug
   $adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"   # not on PATH — use full path
   & $adb shell am start -n com.ssnlva/.MainActivity
   & $adb shell screencap -p /sdcard/screen.png
   & $adb pull /sdcard/screen.png "<abs-path-to-actual.png>"
   ```
   Use `screencap` + `pull`, not `adb exec-out screencap -p > file` — PowerShell's stdout redirection can corrupt binary output.

6. **Compare actual vs. mockup.** Read both PNGs and visually compare text, position, and color. Report any discrepancies plainly — don't paper over a mismatch.

7. **Approval gate #2 — the code.** Only after the screenshot is confirmed to match the approved mockup, explicitly ask the user to approve the code changes, as a separate question from the match confirmation.

---

## Folder Layout

Each verified change gets its own named subfolder — don't share one `mockup`/`actual` pair across changes:

```
ui-verification/<change-name>/
  mockup/
    mockup.html
    mockup.png
  actual/
    actual.png
```

`<change-name>` should be a short kebab/lowercase handle for the change being verified (e.g. `hello` for a "Hello, piAIno" text change).

```
// Good — each change gets its own subfolder
ui-verification/hello/mockup/mockup.png
ui-verification/piano-keyboard/mockup/mockup.png

// Bad — reusing one shared pair, overwriting prior review history
ui-verification/mockup/mockup.png
```

This folder is currently tracked in git (not `.gitignore`d) — leave it that way unless the user says otherwise.