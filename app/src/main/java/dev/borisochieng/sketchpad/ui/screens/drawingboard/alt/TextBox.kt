package dev.borisochieng.sketchpad.ui.screens.drawingboard.alt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset

@Composable
fun MovableTextBox(
    modifier: Modifier = Modifier,
    onRemove: () -> Unit,
    drawMode: DrawMode,

) {
    var text by remember { mutableStateOf("") }
    var offset by remember { mutableStateOf(Offset(100f, 100f)) }
    var activeText by remember { mutableStateOf(true) }
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }


    BoxWithConstraints(
        modifier = modifier
            .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
            .pointerInput(drawMode) {
                // Only detect transform gestures if NOT in drawing modes
                if (drawMode == DrawMode.Text) return@pointerInput  detectTransformGestures { _, pan, zoom, rotationChange ->
                    offset += pan
                    scale *= zoom
                    rotation += rotationChange
                }

            }
    ) {
        val boxWidth = constraints.maxWidth.toFloat()
        val boxHeight = constraints.maxHeight.toFloat()
        if(activeText)  {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .clickable {
                        activeText = true
                    },
                label = {
                    if (text.isEmpty()) {
                        Icon(Icons.Default.Close,
                            contentDescription = "Cancel",
                            modifier = Modifier.clickable {
                                onRemove()
                                activeText = false
                            }
                        )
                    } else {
                        Icon(Icons.Default.Check,
                            contentDescription = "Cancel",
                            modifier = Modifier.clickable {
                                activeText = false
                            }
                        )
                    }
                },
                enabled = activeText,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                )
            )
        }
        else{
            Text(
                text = text,
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .clickable {
                        activeText = true
                    }
            )
        }

    }
}






