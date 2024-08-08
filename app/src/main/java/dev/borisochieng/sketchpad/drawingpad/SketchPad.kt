package dev.borisochieng.sketchpad.drawingpad

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import dev.borisochieng.sketchpad.drawingpad.data.convertToOldColor
import dev.borisochieng.sketchpad.ui.navigation.Screens
import dev.borisochieng.sketchpad.ui.theme.lightScheme
import io.ak1.drawbox.DrawBox
import io.ak1.drawbox.rememberDrawController
import io.ak1.rangvikalp.RangVikalp
import io.ak1.rangvikalp.defaultSelectedColor

@Composable
fun SketchPadScreen(save: (Bitmap) -> Unit, navigate: (Screens) -> Unit ) {
    val undoVisibility = remember { mutableStateOf(false) }
    val redoVisibility = remember { mutableStateOf(false) }
    val colorBarVisibility = remember { mutableStateOf(false) }
    val sizeBarVisibility = remember { mutableStateOf(false) }
    val currentColor = remember { mutableStateOf(defaultSelectedColor) }
    val bg = MaterialTheme.colorScheme.background
    val currentBgColor = remember { mutableStateOf(bg) }
    val currentSize = remember { mutableStateOf(10) }
    val colorIsBg = remember { mutableStateOf(false) }
    val drawController = rememberDrawController()

    Box{
        Column {

            DrawBox(
                drawController = drawController,
                backgroundColor = currentBgColor.value,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f, fill = false),
                bitmapCallback = { imageBitmap, error ->
                    imageBitmap?.let {
                        save(it.asAndroidBitmap())
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
            {
                drawController.saveBitmap()
            },
            {
                colorBarVisibility.value = when (colorBarVisibility.value) {
                    false -> true
                    colorIsBg.value -> true
                    else -> false
                }
                colorIsBg.value = false
                sizeBarVisibility.value = false
            },
            {
                colorBarVisibility.value = when (colorBarVisibility.value) {
                    false -> true
                    !colorIsBg.value -> true
                    else -> false
                }
                colorIsBg.value = true
                sizeBarVisibility.value = false
            },
            {
                sizeBarVisibility.value = !sizeBarVisibility.value
                colorBarVisibility.value = false
            },
            undoVisibility = undoVisibility,
            redoVisibility = redoVisibility,
            colorValue = currentColor,
            bgColorValue = currentBgColor,
            sizeValue = currentSize
        )
        Card(colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
        )  {
            RangVikalp(isVisible = colorBarVisibility.value, showShades = true) {

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
            progress = currentSize.value,
            progressColor = lightScheme.primary.convertToOldColor(),
            thumbColor = currentColor.value.convertToOldColor()
        ) {
            currentSize.value = it
            drawController.changeStrokeWidth(it.toFloat())
        }
    }
}