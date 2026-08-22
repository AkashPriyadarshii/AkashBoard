package com.akashboard.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akashboard.AkashBoardIME
import com.akashboard.core.ShiftState
import kotlinx.coroutines.flow.MutableStateFlow

/** Flat array of [charCode, cx, cy] * 26 consumed by KeyboardLayout for swipe JNI. */
val KeysGlobalState = MutableStateFlow<FloatArray>(FloatArray(0))

private const val SHIFT_LABEL  = "\u21e7"  // ⇧
private const val DELETE_LABEL = "\u232b"  // ⌫
private const val ENTER_LABEL  = "\u21b5"  // ↵
private const val VOICE_LABEL  = "🎤"

@Composable
fun QwertyGrid(ime: AkashBoardIME, modifier: Modifier = Modifier) {

    val rows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf(SHIFT_LABEL, "z", "x", "c", "v", "b", "n", "m", DELETE_LABEL),
        listOf("?123", ",", VOICE_LABEL, " ", ".", ENTER_LABEL)
    )

    val alphaKeysMap = remember { mutableMapOf<String, FloatArray>() }
    var shiftState by remember { mutableStateOf(ShiftState.NONE) }

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        rows.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.Center
            ) {
                rowKeys.forEach { keyLabel ->

                    val weight = when (keyLabel) {
                        " "          -> 4f
                        SHIFT_LABEL, DELETE_LABEL, "?123", ENTER_LABEL -> 1.5f
                        else         -> 1f
                    }

                    val isShifted  = shiftState != ShiftState.NONE
                    val isActionKey = keyLabel == SHIFT_LABEL || keyLabel == DELETE_LABEL || keyLabel == ENTER_LABEL || keyLabel == VOICE_LABEL
                    val displayLabel = when {
                        isShifted && keyLabel.length == 1 && keyLabel[0].isLetter() -> keyLabel.uppercase()
                        keyLabel == " " -> "Space"
                        else -> keyLabel
                    }

                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxHeight()
                            .padding(horizontal = 3.dp, vertical = 5.dp)
                            .onGloballyPositioned { coords ->
                                if (keyLabel.length == 1 && keyLabel[0].isLetter()) {
                                    val pos  = coords.positionInRoot()
                                    val size = coords.size
                                    alphaKeysMap[keyLabel] = floatArrayOf(
                                        keyLabel[0].code.toFloat(),
                                        pos.x + size.width  / 2f,
                                        pos.y + size.height / 2f
                                    )
                                    if (alphaKeysMap.size == 26) {
                                        val flat = FloatArray(78)
                                        var i = 0
                                        for (v in alphaKeysMap.values) { flat[i++]=v[0]; flat[i++]=v[1]; flat[i++]=v[2] }
                                        KeysGlobalState.value = flat
                                    }
                                }
                            }
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isActionKey) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                val ih = ime.inputHandler
                                when (keyLabel) {
                                    DELETE_LABEL -> {
                                        ih?.handleBackspace()
                                        shiftState = ih?.getShiftState() ?: ShiftState.NONE
                                    }
                                    SHIFT_LABEL -> {
                                        shiftState = when (shiftState) {
                                            ShiftState.NONE   -> ShiftState.ONE
                                            ShiftState.ONE    -> ShiftState.LOCKED
                                            ShiftState.LOCKED -> ShiftState.NONE
                                        }
                                        ih?.wordComposer?.setShiftState(shiftState)
                                    }
                                    ENTER_LABEL -> {
                                        ih?.handleEnter()
                                        shiftState = ShiftState.NONE
                                    }
                                    VOICE_LABEL -> {
                                        ime.voiceInput?.startListening()
                                    }
                                    " " -> {
                                        ih?.handleSpace()
                                        if (shiftState == ShiftState.ONE) shiftState = ShiftState.NONE
                                    }
                                    "?123" -> { /* TODO: symbol layout */ }
                                    else -> {
                                        val char = if (isShifted) keyLabel.uppercase().first() else keyLabel.first()
                                        ih?.handleCharacter(char)
                                        if (shiftState == ShiftState.ONE) shiftState = ShiftState.NONE
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayLabel,
                            color = if (isActionKey) MaterialTheme.colorScheme.onSecondaryContainer
                                    else MaterialTheme.colorScheme.onSurface,
                            fontSize = if (keyLabel == " ") 14.sp else 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
