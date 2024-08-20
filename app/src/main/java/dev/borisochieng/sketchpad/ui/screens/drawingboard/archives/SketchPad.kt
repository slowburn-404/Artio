package dev.borisochieng.sketchpad.ui.screens.drawingboard.archives

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.database.convertToOldColor
import dev.borisochieng.sketchpad.ui.navigation.Screens
import dev.borisochieng.sketchpad.ui.screens.dialog.NameSketchDialog
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.SketchPadActions
import dev.borisochieng.sketchpad.ui.screens.drawingboard.utils.DrawMode
import io.ak1.drawbox.DrawBox
import io.ak1.drawbox.rememberDrawController
import io.ak1.rangvikalp.RangVikalp
import io.ak1.rangvikalp.defaultSelectedColor

@Composable
fun SketchPadScreen(
    sketch: Sketch?,
    save: (Bitmap) -> Unit,
    actions: (SketchPadActions) -> Unit,
    navigate: (Screens) -> Unit
) {
    val undoVisibility = remember { mutableStateOf(false) }
    val redoVisibility = remember { mutableStateOf(false) }
    val colorBarVisibility = remember { mutableStateOf(false) }
    val sizeBarVisibility = remember { mutableStateOf(false) }
    val currentColor = remember { mutableStateOf(defaultSelectedColor) }
    val bg = MaterialTheme.colorScheme.background
    val currentBgColor = remember { mutableStateOf(bg) }
    val currentSize = remember { mutableIntStateOf(10) }
    val colorIsBg = remember { mutableStateOf(false) }
    val drawController = rememberDrawController()
    val context = LocalContext.current
    var drawMode by remember { mutableStateOf(DrawMode.Draw) }
    val openNameSketchDialog = rememberSaveable { mutableStateOf(false) }
    var art: Bitmap? = null

    LaunchedEffect(Unit) {
        if (sketch == null) return@LaunchedEffect
//        val drawBoxPayload = DrawBoxPayLoad(
//            sketch.backgroundColor,
//            sketch.pathList.map { it.toPathWrapper() }
//        )
//        drawController.importPath(drawBoxPayload)
    }

    Box {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            val state = rememberTransformableState { zoomChange, panChange, _ ->
                if (drawMode != DrawMode.Touch) return@rememberTransformableState
                scale = (scale * zoomChange).coerceIn(1f, 5f)

                val extraWidth = (scale - 1) * constraints.maxWidth
                val extraHeight = (scale - 1) * constraints.maxHeight

                val maxX = extraWidth / 2
                val maxY = extraHeight / 2

                offset = Offset(
                    x = (offset.x + scale * panChange.x).coerceIn(-maxX, maxX),
                    y = (offset.y + scale * panChange.y).coerceIn(-maxY, maxY)
                )
            }

            DrawBox(
                drawController = drawController,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .transformable(state),
                backgroundColor = currentBgColor.value,
                bitmapCallback = { imageBitmap, _ ->
                    if (imageBitmap == null) {
                        Toast.makeText(context, "No image to save", Toast.LENGTH_SHORT).show()
                        return@DrawBox
                    }
                    art = imageBitmap.asAndroidBitmap()
                    if (sketch == null) {
                        openNameSketchDialog.value = true
                    } else {
                        save(art!!)
                        val payload = drawController.exportPath()
//                        actions(
//                            SketchPadActions.UpdateSketch(
//                                art = art!!,
//                                backgroundColor = payload.bgColor,
//                                paths = payload.path
//                            )
//                        )
                    }
                }
            ) { undoCount, redoCount ->
                sizeBarVisibility.value = false
                colorBarVisibility.value = false
                undoVisibility.value = undoCount != 0
                redoVisibility.value = redoCount != 0
            }

            IconButton(
                onClick = {
                    drawMode = if (drawMode == DrawMode.Touch) DrawMode.Draw else DrawMode.Touch
                    val color = if (drawMode == DrawMode.Touch) Color.Transparent else currentColor.value
                    drawController.changeColor(color)
                },
                modifier = Modifier
                    .padding(20.dp)
                    .size(54.dp)
                    .border(1.dp, Color.Gray, CircleShape),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (drawMode == DrawMode.Touch) Color.Black else Color.White,
                    contentColor = if (drawMode == DrawMode.Touch) Color.White else Color.Gray
                )
            ) {
                Icon(Icons.Rounded.TouchApp, "Touch mode")
            }
        }

        ControlsBar(
            drawController = drawController,
            onDownloadClick = {
                drawController.saveBitmap()
            },
            onColorClick = {
                colorBarVisibility.value = when (colorBarVisibility.value) {
                    false -> true
                    colorIsBg.value -> true
                    else -> false
                }
                colorIsBg.value = false
                sizeBarVisibility.value = false
            },
            onBgColorClick = {
                colorBarVisibility.value = when (colorBarVisibility.value) {
                    false -> true
                    !colorIsBg.value -> true
                    else -> false
                }
                colorIsBg.value = true
                sizeBarVisibility.value = false
            },
            onSizeClick = {
                sizeBarVisibility.value = !sizeBarVisibility.value
                colorBarVisibility.value = false
            },
            undoVisibility = undoVisibility,
            redoVisibility = redoVisibility,
            colorValue = currentColor,
            bgColorValue = currentBgColor,
            sizeValue = currentSize
        )
        Card(colors = cardColors(Color.White))  {
            RangVikalp(
                isVisible = colorBarVisibility.value,
                showShades = true
            ) {
                if (colorIsBg.value) {
                    currentBgColor.value = it
                    drawController.changeBgColor(it)
                } else {
                    currentColor.value = it
                    drawController.changeColor(it)
                }
            }
        }
        CustomSeekbar(
            isVisible = sizeBarVisibility.value,
            progress = currentSize.intValue,
            progressColor = MaterialTheme.colorScheme.primary.convertToOldColor(),
            thumbColor = currentColor.value.convertToOldColor()
        ) {
            currentSize.intValue = it
            drawController.changeStrokeWidth(it.toFloat())
        }

        if (openNameSketchDialog.value) {
            NameSketchDialog(
                onNamed = { name ->
                    if (art == null) {
                        Toast.makeText(context, "No image to save", Toast.LENGTH_SHORT).show()
                        return@NameSketchDialog
                    }
                    val payload = drawController.exportPath()
//                    val newSketch = Sketch(
//                        name = name,
//                        art = art!!,
//                        backgroundColor = payload.bgColor,
//                        pathList = payload.path.map { it.toPath() }
//                    )
//                    actions(SketchPadActions.SaveSketch(newSketch))
                    navigate(Screens.Back)
                },
                onDismiss = { openNameSketchDialog.value = false }
            )
        }
    }
}