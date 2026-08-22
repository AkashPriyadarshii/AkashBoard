package com.akashboard.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akashboard.AkashBoardIME
import com.akashboard.ui.theme.AkashBoardTheme
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.flow.MutableStateFlow

// Global state for live suggestions
val SuggestionsGlobalState = MutableStateFlow<List<String>>(emptyList())

@Composable
fun AkashBoardRoot(ime: AkashBoardIME) {
    val suggestions by SuggestionsGlobalState.collectAsState()

    AkashBoardTheme {
        Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)) {
        // Suggestion Bar
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp).background(MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (suggestions.isEmpty()) {
                Text(text = "AkashBoard v1", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
            } else {
                suggestions.forEach { word ->
                    Text(
                        text = word,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable {
                                ime.currentInputConnection?.commitText(word + " ", 1)
                                SuggestionsGlobalState.value = emptyList() // clear after tap
                            }
                    )
                }
            }
        }
        
        // Main Keyboard Area
        KeyboardLayout(ime = ime, modifier = Modifier.height(250.dp))
        }
    }
}
