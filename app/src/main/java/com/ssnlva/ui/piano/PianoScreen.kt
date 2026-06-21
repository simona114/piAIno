package com.ssnlva.ui.piano

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ssnlva.domain.piano.PianoOctaveLayout
import com.ssnlva.ui.theme.PiAInoTheme
import com.ssnlva.ui.util.LockScreenOrientation

private val FrameBarColor = Color(0xFF1C1B1F)
private val FrameBarBorderColor = Color(0xFF3A3A3D)
private val WhiteKeyColor = Color(0xFFFFFBFE)
private val BlackKeyColor = Color(0xFF1C1B1F)

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
 * Layout-only proof-of-concept: no labels, no scrolling, no touch handling, no audio.
 */
@Composable
fun PianoScreen(modifier: Modifier = Modifier) {
    LockScreenOrientation()

    val keys = PianoOctaveLayout.keys
    val whiteKeyCount = keys.count { !it.isBlack }

    Canvas(modifier = modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
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
        for (index in 0 until whiteKeyCount) {
            val left = index * whiteKeyWidth
            drawRect(
                color = WhiteKeyColor,
                topLeft = Offset(left, keyboardTop),
                size = Size(whiteKeyWidth, keyboardHeight)
            )
            drawRect(
                color = BlackKeyColor,
                topLeft = Offset(left, keyboardTop),
                size = Size(whiteKeyWidth, keyboardHeight),
                style = Stroke(width = borderWidthPx)
            )
        }

        // Black keys, drawn after the white keys so the overlap renders correctly. Each black
        // key is centered on the boundary after a given white-key index.
        val blackKeyWidth = whiteKeyWidth * BlackKeyWidthFraction
        val blackKeyHeight = keyboardHeight * BlackKeyHeightFraction
        var whiteKeyIndex = -1
        for (key in keys) {
            if (!key.isBlack) {
                whiteKeyIndex++
                continue
            }
            val boundaryX = (whiteKeyIndex + 1) * whiteKeyWidth
            val left = boundaryX - blackKeyWidth / 2f
            drawRect(
                color = BlackKeyColor,
                topLeft = Offset(left, keyboardTop),
                size = Size(blackKeyWidth, blackKeyHeight)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 360)
@Composable
private fun PianoScreenPreview() {
    PiAInoTheme {
        PianoScreen()
    }
}
