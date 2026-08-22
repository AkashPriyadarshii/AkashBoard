package com.akashboard.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.akashboard.AkashBoardIME
import com.akashboard.engine.PredictorBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun KeyboardLayout(ime: AkashBoardIME, modifier: Modifier = Modifier) {
    val swipePoints = remember { mutableStateListOf<Offset>() }
    val scope = remember { CoroutineScope(Dispatchers.Default) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        swipePoints.clear()
                        swipePoints.add(offset)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        swipePoints.add(change.position)
                    },
                    onDragEnd = {
                        if (swipePoints.size > 2) {
                                                        val pathArray = FloatArray(swipePoints.size * 2)
                            for (i in swipePoints.indices) {
                                pathArray[i * 2] = swipePoints[i].x
                                pathArray[i * 2 + 1] = swipePoints[i].y
                            }
                            
                            // Dummy keys array for now (x, y, radius, char_code) for each key
                            // This must be populated by QwertyGrid later.
                            val keysArray = KeysGlobalState.value 

                            scope.launch {
                                try {
                                    val topK = 5
                                    val suggestions = PredictorBridge.recognizeSwipe(pathArray, keysArray, topK)
                                    if (suggestions.isNotEmpty()) {
                                        val topWord = suggestions[0]
                                        SuggestionsGlobalState.value = suggestions
                                        // Commit top swipe result + space
                                        ime.currentInputConnection?.commitText("$topWord ", 1)
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("KeyboardLayout", "Swipe JNI crash", e)
                                }
                            }
                        }
                        swipePoints.clear()
                    },
                    onDragCancel = {
                        swipePoints.clear()
                    }
                )
            }
    ) {
        // Keys grid — renders below swipe trail overlay
        QwertyGrid(ime = ime, modifier = Modifier.fillMaxSize())

        Canvas(modifier = Modifier.fillMaxSize()) {
            if (swipePoints.size > 1) {
                val path = Path()
                path.moveTo(swipePoints.first().x, swipePoints.first().y)

                for (i in 1 until swipePoints.size - 1) {
                    val p0 = swipePoints[i]
                    val p1 = swipePoints[i + 1]
                    val midX = (p0.x + p1.x) / 2f
                    val midY = (p0.y + p1.y) / 2f
                    path.quadraticBezierTo(p0.x, p0.y, midX, midY)
                }

                path.lineTo(swipePoints.last().x, swipePoints.last().y)

                drawPath(
                    path = path,
                    color = Color(0xFF00FFCC),
                    style = Stroke(
                        width = 12f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
