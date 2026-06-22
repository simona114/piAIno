package com.ssnlva.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ssnlva.domain.piano.Notation
import com.ssnlva.ui.theme.PiAInoTheme

/**
 * Settings screen with a black toolbar/white body, distinct from the app's main theme.
 * Holds preferences for whether note-name labels are shown on the piano's white keys, and
 * which notation (letters or solfège) those labels use.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    showNoteNames: Boolean,
    onShowNoteNamesChange: (Boolean) -> Unit,
    notation: Notation,
    onNotationChange: (Notation) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Show note names", color = Color.Black)
                Switch(
                    checked = showNoteNames,
                    onCheckedChange = onShowNoteNamesChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.Black,
                        checkedBorderColor = Color.Black
                    )
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Note notation", color = Color.Black)
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = notation == Notation.LETTER,
                        onClick = { onNotationChange(Notation.LETTER) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Color.Black,
                            activeContentColor = Color.White,
                            inactiveContainerColor = Color.White,
                            inactiveContentColor = Color.Black,
                            activeBorderColor = Color.Black,
                            inactiveBorderColor = Color.Black
                        )
                    ) {
                        Text("Letters")
                    }
                    SegmentedButton(
                        selected = notation == Notation.SOLFEGE,
                        onClick = { onNotationChange(Notation.SOLFEGE) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Color.Black,
                            activeContentColor = Color.White,
                            inactiveContainerColor = Color.White,
                            inactiveContentColor = Color.Black,
                            activeBorderColor = Color.Black,
                            inactiveBorderColor = Color.Black
                        )
                    ) {
                        Text("Solfège")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun SettingsScreenPreview() {
    PiAInoTheme {
        SettingsScreen(
            showNoteNames = true,
            onShowNoteNamesChange = {},
            notation = Notation.LETTER,
            onNotationChange = {},
            onBack = {}
        )
    }
}
