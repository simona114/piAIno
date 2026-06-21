package com.ssnlva.ui.piano

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.ssnlva.domain.piano.PianoKey
import com.ssnlva.domain.piano.PianoKeyboardLayout
import com.ssnlva.domain.piano.displayName
import com.ssnlva.ui.theme.PiAInoTheme
import com.ssnlva.ui.util.LockScreenOrientation

private val FrameBarColor = Color(0xFF1C1B1F)
private val FrameBarBorderColor = Color(0xFF3A3A3D)
private val WhiteKeyColor = Color(0xFFFFFBFE)
private val WhiteKeyPressedColor = Color(0xFFC9C9C9)
private val WhiteKeyBorderColor = Color(0xFF1C1B1F)
private val BlackKeyColor = Color(0xFF3A3A3D)
private val BlackKeyPressedColor = Color(0xFF1C1B1F)

private const val FrameBarHeightFraction = 0.1165f
private const val BlackKeyWidthFraction = 0.6f
private const val BlackKeyHeightFraction = 0.6f
private const val WhiteKeyBorderWidthDp = 2f
private const val FrameBarBorderWidthDp = 3f

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
 * held key is highlighted and toasts its note name once on press-down.
 *
 * If a pointer drifts past touch-slop while holding a key, that pointer's own key releases and
 * it starts panning the keyboard instead - this is intended (a drifting finger should scroll,
 * not protect its held note). Other pointers' held keys are unaffected by another pointer's pan.
 *
 * Layout-only proof-of-concept: no labels, no audio.
 */
@Composable
fun PianoScreen(modifier: Modifier = Modifier) {
    LockScreenOrientation()

    val context = LocalContext.current
    val density = LocalDensity.current
    val keys = PianoKeyboardLayout.keys
    val whiteKeyCount = remember(keys) { keys.count { !it.isBlack } }
    val whiteKeyWidthPx = remember(density) { with(density) { WhiteKeyWidthDp.dp.toPx() } }
    val centerWhiteKeyIndex = remember(keys) {
        keys.subList(0, PianoKeyboardLayout.centerKeyIndex).count { !it.isBlack }
    }

    var pressedKeyIndices by remember { mutableStateOf(emptySet<Int>()) }
    var scrollOffsetPx by remember { mutableFloatStateOf(-1f) } // -1 = not yet initialized
    val pointerStates = remember { mutableMapOf<PointerId, PointerKeyState>() }

    fun maxScrollOffsetPx(viewportWidthPx: Float): Float =
        (whiteKeyCount * whiteKeyWidthPx - viewportWidthPx).coerceAtLeast(0f)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
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
                                        Toast.makeText(context, keys[keyIndex].displayName, Toast.LENGTH_SHORT).show()
                                    }
                                    change.consume()
                                }

                                change.changedToUp() -> {
                                    val keyIndex = pointerStates.remove(change.id)?.keyIndex
                                    if (keyIndex != null) {
                                        pressedKeyIndices = pressedKeyIndices - keyIndex
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
        PianoScreen()
    }
}
