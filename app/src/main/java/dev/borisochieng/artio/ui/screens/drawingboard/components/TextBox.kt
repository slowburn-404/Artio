package dev.borisochieng.artio.ui.screens.drawingboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import dev.borisochieng.model.TextProperties
import java.util.UUID.randomUUID

@Composable
fun MovableTextBox(
    modifier: Modifier = Modifier,
    properties: dev.borisochieng.model.TextProperties = dev.borisochieng.model.TextProperties(),
    active: Boolean,
    onRemove: (dev.borisochieng.model.TextProperties) -> Unit,
    onFinish: (dev.borisochieng.model.TextProperties) -> Unit = {},
    onUpdate: (dev.borisochieng.model.TextProperties) -> Unit = {}
) {
    var text by remember { mutableStateOf(properties.text) }
    var offset by remember { mutableStateOf(properties.offset) }
    var scale by remember { mutableFloatStateOf(properties.scale) }
    var rotation by remember { mutableFloatStateOf(properties.rotation) }
    var activeText by remember(active) { mutableStateOf(active) }

    Box(
        modifier = modifier
            .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
            .pointerInput(activeText) {
                // Only detect transform gestures if NOT in drawing modes
                if (!activeText) return@pointerInput
                detectTransformGestures { _, pan, zoom, rotationChange ->
                    offset += pan
                    scale *= zoom
                    rotation += rotationChange
                }
            }
    ) {
        if (activeText) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .wrapContentSize()
                    .clickable { activeText = true },
                label = {
                    Icon(
                        imageVector = if (text.isEmpty()) Icons.Default.Close else Icons.Default.Check,
                        contentDescription = if (text.isEmpty()) "Cancel" else "Save",
                        modifier = Modifier.clickable {
                            activeText = false
                            if (text.isEmpty()) {
                                onRemove(properties)
                                return@clickable
                            }
                            val textIsNew = properties.id.isEmpty()
                            val textProperties = dev.borisochieng.model.TextProperties(
                                id = if (textIsNew) randomUUID().toString() else properties.id,
                                text = text, offset = offset,
                                scale = scale, rotation = rotation
                            )
                            if (textIsNew) onFinish(textProperties) else onUpdate(textProperties)
                        }
                    )
                },
                enabled = activeText,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                )
            )
        } else {
            Text(
                text = text,
                modifier = Modifier
                    .wrapContentSize()
                    .clickable { activeText = true }
            )
        }
    }
}
