package dev.borisochieng.sketchpad.ui.screens.drawingboard

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.ui.navigation.Screens
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.convertToOldColor
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
    val openNameSketchDialog = rememberSaveable { mutableStateOf(false) }
    var art: Bitmap? = null

    Box {
        Column {
            DrawBox(
                drawController = drawController,
                backgroundColor = currentBgColor.value,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f, fill = false),
                bitmapCallback = { imageBitmap, error ->
                    if (imageBitmap == null) {
                        Toast.makeText(context, "No image to save", Toast.LENGTH_SHORT).show()
                        return@DrawBox
                    }
                    art = imageBitmap.asAndroidBitmap()
                    if (sketch == null) {
                        openNameSketchDialog.value = true
                    } else {
                        save(art!!)
                        actions(SketchPadActions.UpdateSketch(art!!))
                    }
                }
            ) { undoCount, redoCount ->
                sizeBarVisibility.value = false
                colorBarVisibility.value = false
                undoVisibility.value = undoCount != 0
                redoVisibility.value = redoCount != 0
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

        if (openNameSketchDialog.value && art != null) {
            NameSketchDialog(
                art = art!!,
                onNamed = {
                    actions(SketchPadActions.SaveSketch(it))
                    navigate(Screens.Back)
                },
                onDismiss = { openNameSketchDialog.value = false }
            )
        }
    }
}