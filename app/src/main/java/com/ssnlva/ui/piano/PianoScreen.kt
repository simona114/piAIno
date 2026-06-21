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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.ssnlva.domain.piano.PianoKey
import com.ssnlva.domain.piano.PianoOctaveLayout
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

/**
 * Renders exactly one octave of a piano keyboard, full-bleed: a frame bar strip above the
 * keyboard, 7 white keys tiling the full width, and 5 black keys overlapping the boundaries
 * between the appropriate white keys.
 *
 * Each key is tappable independently and multiple keys can be held at once (one pointer per
 * key), since this is a piano and chords are the normal case, not the edge case. A held key
 * is highlighted and toasts its note name once on press-down.
 *
 * Layout-only proof-of-concept: no labels, no scrolling, no audio.
 */
@Composable
fun PianoScreen(modifier: Modifier = Modifier) {
    LockScreenOrientation()

    val context = LocalContext.current
    val keys = PianoOctaveLayout.keys
    val whiteKeyCount = keys.count { !it.isBlack }
    var pressedKeyIndices by remember { mutableStateOf(emptySet<Int>()) }
    val pressedPointers = remember { mutableMapOf<PointerId, Int>() }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .pointerInput(keys) {
                awaitEachGesture {
                    do {
                        val event = awaitPointerEvent()
                        for (change in event.changes) {
                            if (change.changedToDown()) {
                                val keyIndex = hitTestKey(change.position, size.toSize(), keys)
                                if (keyIndex != null) {
                                    pressedPointers[change.id] = keyIndex
                                    pressedKeyIndices = pressedKeyIndices + keyIndex
                                    Toast.makeText(context, keys[keyIndex].name, Toast.LENGTH_SHORT).show()
                                }
                                change.consume()
                            } else if (change.changedToUp()) {
                                val keyIndex = pressedPointers.remove(change.id)
                                if (keyIndex != null) {
                                    pressedKeyIndices = pressedKeyIndices - keyIndex
                                }
                                change.consume()
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        val frameBarHeight = size.height * FrameBarHeightFraction
        val keyboardTop = frameBarHeight
        val keyboardHeight = size.height - frameBarHeight
        val whiteKeyWidth = size.width / whiteKeyCount
        val borderWidthPx = WhiteKeyBorderWidthDp.dp.toPx()
        val frameBarBorderWidthPx = FrameBarBorderWidthDp.dp.toPx()

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

        // White keys, drawn first so black keys can overlap them.
        var whiteKeyIndex = -1
        for ((index, key) in keys.withIndex()) {
            if (key.isBlack) continue
            whiteKeyIndex++
            val left = whiteKeyIndex * whiteKeyWidth
            drawRect(
                color = if (index in pressedKeyIndices) WhiteKeyPressedColor else WhiteKeyColor,
                topLeft = Offset(left, keyboardTop),
                size = Size(whiteKeyWidth, keyboardHeight)
            )
            drawRect(
                color = WhiteKeyBorderColor,
                topLeft = Offset(left, keyboardTop),
                size = Size(whiteKeyWidth, keyboardHeight),
                style = Stroke(width = borderWidthPx)
            )
        }

        // Black keys, drawn after the white keys so the overlap renders correctly. Each black
        // key is centered on the boundary after a given white-key index.
        val blackKeyWidth = whiteKeyWidth * BlackKeyWidthFraction
        val blackKeyHeight = keyboardHeight * BlackKeyHeightFraction
        whiteKeyIndex = -1
        for ((index, key) in keys.withIndex()) {
            if (!key.isBlack) {
                whiteKeyIndex++
                continue
            }
            val boundaryX = (whiteKeyIndex + 1) * whiteKeyWidth
            val left = boundaryX - blackKeyWidth / 2f
            drawRect(
                color = if (index in pressedKeyIndices) BlackKeyPressedColor else BlackKeyColor,
                topLeft = Offset(left, keyboardTop),
                size = Size(blackKeyWidth, blackKeyHeight)
            )
        }
    }
}

/**
 * Mirrors the layout math above to find which key (if any) a touch landed on, checking black
 * keys first since they're drawn on top of the white keys they overlap.
 */
private fun hitTestKey(position: Offset, canvasSize: Size, keys: List<PianoKey>): Int? {
    val frameBarHeight = canvasSize.height * FrameBarHeightFraction
    if (position.y < frameBarHeight) return null

    val keyboardTop = frameBarHeight
    val keyboardHeight = canvasSize.height - frameBarHeight
    val whiteKeyCount = keys.count { !it.isBlack }
    val whiteKeyWidth = canvasSize.width / whiteKeyCount
    val blackKeyWidth = whiteKeyWidth * BlackKeyWidthFraction
    val blackKeyHeight = keyboardHeight * BlackKeyHeightFraction

    if (position.y <= keyboardTop + blackKeyHeight) {
        var whiteKeyIndex = -1
        for ((index, key) in keys.withIndex()) {
            if (!key.isBlack) {
                whiteKeyIndex++
                continue
            }
            val boundaryX = (whiteKeyIndex + 1) * whiteKeyWidth
            val left = boundaryX - blackKeyWidth / 2f
            if (position.x in left..(left + blackKeyWidth)) return index
        }
    }

    val tappedWhiteColumn = (position.x / whiteKeyWidth).toInt().coerceIn(0, whiteKeyCount - 1)
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
