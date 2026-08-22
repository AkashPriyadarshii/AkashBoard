package com.akashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akashboard.settings.KeyboardSettingsProvider
import com.akashboard.ui.theme.AkashBoardTheme

class SettingsActivity : ComponentActivity() {
    private lateinit var settings: KeyboardSettingsProvider

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = KeyboardSettingsProvider(this)

        setContent {
            AkashBoardTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("AkashBoard Settings", fontWeight = FontWeight.Bold) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    }
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        
                        item { SettingsSectionTitle("Typing & Input") }
                        item { 
                            SettingsCard {
                                BooleanSetting(
                                    title = "Swipe Typing",
                                    subtitle = "Glide across letters to type words",
                                    initialValue = settings.swipeTypingEnabled,
                                    onCheckedChange = { settings.swipeTypingEnabled = it }
                                )
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                BooleanSetting(
                                    title = "Predictive Text",
                                    subtitle = "Show word suggestions while typing",
                                    initialValue = settings.predictiveTextEnabled,
                                    onCheckedChange = { settings.predictiveTextEnabled = it }
                                )
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                BooleanSetting(
                                    title = "Auto-Correct",
                                    subtitle = "Automatically fix typos using neural engine",
                                    initialValue = settings.autoCorrectEnabled,
                                    onCheckedChange = { settings.autoCorrectEnabled = it }
                                )
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                BooleanSetting(
                                    title = "Spacebar Cursor Control",
                                    subtitle = "Slide on spacebar to move cursor",
                                    initialValue = settings.spacebarCursorEnabled,
                                    onCheckedChange = { settings.spacebarCursorEnabled = it }
                                )
                            }
                        }

                        item { SettingsSectionTitle("Appearance & Layout") }
                        item {
                            SettingsCard {
                                SliderSetting(
                                    title = "Keyboard Height",
                                    value = settings.keyboardHeight.toFloat(),
                                    range = 200f..400f,
                                    onValueChange = { settings.keyboardHeight = it.toInt() }
                                )
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                SliderSetting(
                                    title = "Key Corner Radius",
                                    value = settings.keyCornerRadius.toFloat(),
                                    range = 0f..24f,
                                    onValueChange = { settings.keyCornerRadius = it.toInt() }
                                )
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                BooleanSetting(
                                    title = "Show Number Row",
                                    subtitle = "Keep numbers visible at the top",
                                    initialValue = settings.showNumberRow,
                                    onCheckedChange = { settings.showNumberRow = it }
                                )
                            }
                        }

                        item { SettingsSectionTitle("Feedback") }
                        item {
                            SettingsCard {
                                BooleanSetting(
                                    title = "Haptic Feedback",
                                    subtitle = "Vibrate on keypress",
                                    initialValue = settings.vibrateOnKeypress,
                                    onCheckedChange = { settings.vibrateOnKeypress = it }
                                )
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                BooleanSetting(
                                    title = "Sound on Keypress",
                                    subtitle = "Play a click sound",
                                    initialValue = settings.soundOnKeypress,
                                    onCheckedChange = { settings.soundOnKeypress = it }
                                )
                            }
                        }

                        item { SettingsSectionTitle("Privacy & Engine") }
                        item {
                            SettingsCard {
                                BooleanSetting(
                                    title = "Incognito Mode",
                                    subtitle = "Disable learning and telemetry entirely",
                                    initialValue = settings.incognitoMode,
                                    onCheckedChange = { settings.incognitoMode = it }
                                )
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                BooleanSetting(
                                    title = "Personal Learning",
                                    subtitle = "Learn your typing patterns locally",
                                    initialValue = settings.learningEnabled,
                                    onCheckedChange = { settings.learningEnabled = it }
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        content = content
    )
}

@Composable
fun BooleanSetting(
    title: String,
    subtitle: String,
    initialValue: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    var checked by remember { mutableStateOf(initialValue) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = { 
                checked = it
                onCheckedChange(it)
            }
        )
    }
}

@Composable
fun SliderSetting(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    var currentValue by remember { mutableStateOf(value) }
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "${currentValue.toInt()}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(
            value = currentValue,
            onValueChange = { currentValue = it },
            onValueChangeFinished = { onValueChange(currentValue) },
            valueRange = range
        )
    }
}
