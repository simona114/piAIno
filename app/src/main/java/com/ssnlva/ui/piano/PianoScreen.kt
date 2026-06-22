package com.ssnlva.ui.piano

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.ssnlva.domain.piano.Notation
import com.ssnlva.domain.piano.PianoKey
import com.ssnlva.domain.piano.PianoKeyboardLayout
import com.ssnlva.domain.piano.labelFor
import com.ssnlva.ui.theme.PiAInoTheme
import com.ssnlva.ui.util.LockScreenOrientation

private val FrameBarColor = Color(0xFF1C1B1F)
private val FrameBarBorderColor = Color(0xFF3A3A3D)
private val WhiteKeyColor = Color(0xFFFFFBFE)
private val WhiteKeyPressedColor = Color(0xFFC9C9C9)
private val WhiteKeyBorderColor = Color(0xFF1C1B1F)
private val BlackKeyColor = Color(0xFF3A3A3D)
private val BlackKeyPressedColor = Color(0xFF1C1B1F)

private const val FrameBarHeightFraction = 0.18f
private const val BlackKeyWidthFraction = 0.6f
private const val BlackKeyHeightFraction = 0.6f
private const val WhiteKeyBorderWidthDp = 2f
private const val FrameBarBorderWidthDp = 3f
private const val NoteLabelFontSizeSp = 24f
private const val NoteLabelBottomPaddingDp = 10f
private const val SettingsButtonSizeDp = 48f
private const val SettingsButtonCornerRadiusDp = 8f
private const val SettingsButtonEndPaddingDp = 12f
private const val SustainLabelSwitchSpacingDp = 8f

// Tuned so roughly one octave fills a typical landscape phone viewport, matching the look of
// the original single-octave proof-of-concept. Not architecturally load-bearing.
private const val WhiteKeyWidthDp = 90f

/** Tracks one active touch: where it went down, which key (if any) it's holding, and whether
 *  it has drifted past touch-slop into panning the keyboard instead. */
private class PointerKeyState(val downPosition: Offset, var keyIndex: Int?, var isPanning: Boolean)

/**
 * Renders the full 88-key (A0-C8) piano keyboard, horizontally scrollable, with a frame bar
 * strip above the keys. Keys are tappable independently and multiple keys can be held at once
 * (one pointer per key), since chords are the normal case for a piano, not the edge case. A
 * held key is highlighted and plays its note once on press-down.
 *
 * If a pointer drifts past touch-slop while holding a key, that pointer's own key releases and
 * it starts panning the keyboard instead - this is intended (a drifting finger should scroll,
 * not protect its held note). Other pointers' held keys are unaffected by another pointer's pan.
 *
 * When [showNoteNames] is true, each white key is labeled with its note name near the
 * bottom of the key, under the [notation] (letters or solfège) chosen by the user, colored per
 * pitch class via [NoteLetterColors]; black keys are never labeled.
 * A square settings button sits on the frame bar and invokes [onSettingsClick] to open the
 * preferences screen that controls [showNoteNames] and [notation].
 */
@Composable
fun PianoScreen(
    showNoteNames: Boolean,
    notation: Notation,
    sustainEnabled: Boolean,
    onSettingsClick: () -> Unit,
    onSustainToggle: () -> Unit,
    onKeyPressed: (Int) -> Unit,
    onKeyReleased: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LockScreenOrientation()

    val density = LocalDensity.current
    val keys = PianoKeyboardLayout.keys
    val whiteKeyCount = remember(keys) { keys.count { !it.isBlack } }
    val whiteKeyWidthPx = remember(density) { with(density) { WhiteKeyWidthDp.dp.toPx() } }
    val centerWhiteKeyIndex = remember(keys) {
        keys.subList(0, PianoKeyboardLayout.centerKeyIndex).count { !it.isBlack }
    }
    val textMeasurer = rememberTextMeasurer()
    val noteLabelBottomPaddingPx = remember(density) { with(density) { NoteLabelBottomPaddingDp.dp.toPx() } }
    // Each letter's TextLayoutResult is measured once and reused every frame - measuring text
    // is expensive, and this draw scope re-runs continuously while panning.
    val noteLetterLayouts = remember(textMeasurer, notation) {
        NoteLetterColors.mapValues { (letter, color) ->
            textMeasurer.measure(
                text = notation.labelFor(letter),
                style = TextStyle(color = color, fontSize = NoteLabelFontSizeSp.sp, fontWeight = FontWeight.Bold)
            )
        }
    }

    var pressedKeyIndices by remember { mutableStateOf(emptySet<Int>()) }
    var scrollOffsetPx by remember { mutableFloatStateOf(-1f) } // -1 = not yet initialized
    val pointerStates = remember { mutableMapOf<PointerId, PointerKeyState>() }

    fun maxScrollOffsetPx(viewportWidthPx: Float): Float =
        (whiteKeyCount * whiteKeyWidthPx - viewportWidthPx).coerceAtLeast(0f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val frameBarHeightDp = maxHeight * FrameBarHeightFraction

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    if (scrollOffsetPx < 0f) {
                        val centerWhiteKeyLeft = centerWhiteKeyIndex * whiteKeyWidthPx
                        val target = centerWhiteKeyLeft - size.width / 2f + whiteKeyWidthPx / 2f
                        scrollOffsetPx = target.coerceIn(0f, maxScrollOffsetPx(size.width.toFloat()))
                    }
                }
                .pointerInput(keys) {
                    val touchSlop = viewConfiguration.touchSlop
                    awaitEachGesture {
                        do {
                            val event = awaitPointerEvent()
                            for (change in event.changes) {
                                when {
                                    change.changedToDown() -> {
                                        val keyIndex = hitTestKey(
                                            position = change.position,
                                            canvasSize = size.toSize(),
                                            keys = keys,
                                            whiteKeyWidthPx = whiteKeyWidthPx,
                                            scrollOffsetPx = scrollOffsetPx
                                        )
                                        pointerStates[change.id] =
                                            PointerKeyState(change.position, keyIndex, isPanning = false)
                                        if (keyIndex != null) {
                                            pressedKeyIndices = pressedKeyIndices + keyIndex
                                            onKeyPressed(keys[keyIndex].midiNote)
                                        }
                                        change.consume()
                                    }

                                    change.changedToUp() -> {
                                        val keyIndex = pointerStates.remove(change.id)?.keyIndex
                                        if (keyIndex != null) {
                                            pressedKeyIndices = pressedKeyIndices - keyIndex
                                            onKeyReleased(keys[keyIndex].midiNote)
                                        }
                                        change.consume()
                                    }

                                    change.pressed -> {
                                        val pointerState = pointerStates[change.id]
                                        if (pointerState != null) {
                                            if (pointerState.isPanning) {
                                                val deltaX = change.position.x - change.previousPosition.x
                                                scrollOffsetPx = (scrollOffsetPx - deltaX)
                                                    .coerceIn(0f, maxScrollOffsetPx(size.width.toFloat()))
                                                change.consume()
                                            } else {
                                                val heldKeyIndex = pointerState.keyIndex
                                                val drift = (change.position - pointerState.downPosition).getDistance()
                                                if (heldKeyIndex != null && drift > touchSlop) {
                                                    pressedKeyIndices = pressedKeyIndices - heldKeyIndex
                                                    onKeyReleased(keys[heldKeyIndex].midiNote)
                                                    pointerState.keyIndex = null
                                                    pointerState.isPanning = true
                                                    change.consume()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
        ) {
            val effectiveScrollOffsetPx = scrollOffsetPx.coerceAtLeast(0f)
            val frameBarHeight = size.height * FrameBarHeightFraction
            val keyboardTop = frameBarHeight
            val keyboardHeight = size.height - frameBarHeight
            val borderWidthPx = WhiteKeyBorderWidthDp.dp.toPx()
            val frameBarBorderWidthPx = FrameBarBorderWidthDp.dp.toPx()
            val blackKeyWidthPx = whiteKeyWidthPx * BlackKeyWidthFraction
            val blackKeyHeight = keyboardHeight * BlackKeyHeightFraction

            // Frame bar strip above the keyboard.
            drawRect(
                color = FrameBarColor,
                topLeft = Offset(0f, 0f),
                size = Size(size.width, frameBarHeight)
            )
            drawLine(
                color = FrameBarBorderColor,
                start = Offset(0f, frameBarHeight - frameBarBorderWidthPx / 2f),
                end = Offset(size.width, frameBarHeight - frameBarBorderWidthPx / 2f),
                strokeWidth = frameBarBorderWidthPx
            )

            // White keys, drawn first so black keys can overlap them. Off-screen keys are skipped.
            var whiteKeyIndex = -1
            for ((index, key) in keys.withIndex()) {
                if (key.isBlack) continue
                whiteKeyIndex++
                val left = whiteKeyIndex * whiteKeyWidthPx - effectiveScrollOffsetPx
                if (left + whiteKeyWidthPx < 0f || left > size.width) continue
                drawRect(
                    color = if (index in pressedKeyIndices) WhiteKeyPressedColor else WhiteKeyColor,
                    topLeft = Offset(left, keyboardTop),
                    size = Size(whiteKeyWidthPx, keyboardHeight)
                )
                drawRect(
                    color = WhiteKeyBorderColor,
                    topLeft = Offset(left, keyboardTop),
                    size = Size(whiteKeyWidthPx, keyboardHeight),
                    style = Stroke(width = borderWidthPx)
                )
                if (showNoteNames) {
                    val textLayout = noteLetterLayouts.getValue(key.name)
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(
                            left + whiteKeyWidthPx / 2f - textLayout.size.width / 2f,
                            keyboardTop + keyboardHeight - noteLabelBottomPaddingPx - textLayout.size.height
                        )
                    )
                }
            }

            // Black keys, drawn after the white keys so the overlap renders correctly. Each black
            // key is centered on the boundary after a given white-key index.
            whiteKeyIndex = -1
            for ((index, key) in keys.withIndex()) {
                if (!key.isBlack) {
                    whiteKeyIndex++
                    continue
                }
                val boundaryX = (whiteKeyIndex + 1) * whiteKeyWidthPx
                val left = boundaryX - blackKeyWidthPx / 2f - effectiveScrollOffsetPx
                if (left + blackKeyWidthPx < 0f || left > size.width) continue
                drawRect(
                    color = if (index in pressedKeyIndices) BlackKeyPressedColor else BlackKeyColor,
                    topLeft = Offset(left, keyboardTop),
                    size = Size(blackKeyWidthPx, blackKeyHeight)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = SettingsButtonEndPaddingDp.dp,
                    top = (frameBarHeightDp - SettingsButtonSizeDp.dp) / 2f
                )
                .height(SettingsButtonSizeDp.dp)
        ) {
            Text(text = "Sustain", color = WhiteKeyColor)
            Spacer(modifier = Modifier.width(SustainLabelSwitchSpacingDp.dp))
            Switch(
                checked = sustainEnabled,
                onCheckedChange = { onSustainToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BlackKeyColor,
                    checkedTrackColor = WhiteKeyColor,
                    checkedBorderColor = WhiteKeyColor,
                    uncheckedThumbColor = WhiteKeyColor,
                    uncheckedTrackColor = Color.Transparent,
                    uncheckedBorderColor = WhiteKeyColor
                )
            )
        }

        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = WhiteKeyColor,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    end = SettingsButtonEndPaddingDp.dp,
                    top = (frameBarHeightDp - SettingsButtonSizeDp.dp) / 2f
                )
                .size(SettingsButtonSizeDp.dp)
                .clip(RoundedCornerShape(SettingsButtonCornerRadiusDp.dp))
                .background(BlackKeyColor)
                .clickable(onClick = onSettingsClick)
                .padding(12.dp)
        )
    }
}

/**
 * Mirrors the layout math above to find which key (if any) a touch landed on, checking black
 * keys first since they're drawn on top of the white keys they overlap. [scrollOffsetPx]
 * converts the tap's viewport-space x into the same content-space x the draw loop positions
 * keys in.
 */
private fun hitTestKey(
    position: Offset,
    canvasSize: Size,
    keys: List<PianoKey>,
    whiteKeyWidthPx: Float,
    scrollOffsetPx: Float
): Int? {
    val frameBarHeight = canvasSize.height * FrameBarHeightFraction
    if (position.y < frameBarHeight) return null

    val keyboardTop = frameBarHeight
    val keyboardHeight = canvasSize.height - frameBarHeight
    val blackKeyWidthPx = whiteKeyWidthPx * BlackKeyWidthFraction
    val blackKeyHeight = keyboardHeight * BlackKeyHeightFraction
    val contentX = position.x + scrollOffsetPx.coerceAtLeast(0f)

    if (position.y <= keyboardTop + blackKeyHeight) {
        var whiteKeyIndex = -1
        for ((index, key) in keys.withIndex()) {
            if (!key.isBlack) {
                whiteKeyIndex++
                continue
            }
            val boundaryX = (whiteKeyIndex + 1) * whiteKeyWidthPx
            val left = boundaryX - blackKeyWidthPx / 2f
            if (contentX in left..(left + blackKeyWidthPx)) return index
        }
    }

    val tappedWhiteColumn = (contentX / whiteKeyWidthPx).toInt()
    var whiteKeyIndex = -1
    for ((index, key) in keys.withIndex()) {
        if (!key.isBlack) {
            whiteKeyIndex++
            if (whiteKeyIndex == tappedWhiteColumn) return index
        }
    }
    return null
}

@Preview(showBackground = true, widthDp = 800, heightDp = 360)
@Composable
private fun PianoScreenPreview() {
    PiAInoTheme {
        PianoScreen(
            showNoteNames = true,
            notation = Notation.LETTER,
            sustainEnabled = true,
            onSettingsClick = {},
            onSustainToggle = {},
            onKeyPressed = {},
            onKeyReleased = {}
        )
    }
}
