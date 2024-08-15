package dev.borisochieng.sketchpad.ui.screens.drawingboard.alt

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.LineWeight
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.borisochieng.sketchpad.R
import dev.borisochieng.sketchpad.ui.screens.dialog.ColorPickerDialog
import dev.borisochieng.sketchpad.ui.screens.dialog.SizePickerDialog

@Composable
fun PaletteMenu(
    modifier: Modifier = Modifier,
    drawMode: DrawMode,
    selectedColor: Color,
    pencilSize: Float,
    onColorChanged: (Color) -> Unit,
    onSizeChanged: (Float) -> Unit,
    onDrawModeChanged: (DrawMode) -> Unit,
) {
    var currentDrawMode = drawMode
    val openColorPickerDialog = remember { mutableStateOf(false) }
    val openSizePickerDialog = remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(MaterialTheme.shapes.large)
            .background(Color.LightGray)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                currentDrawMode =
                    if (currentDrawMode == DrawMode.Touch) DrawMode.Draw else DrawMode.Touch
                onDrawModeChanged(currentDrawMode)
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.TouchApp,
                contentDescription = "Touch mode",
                tint = if (currentDrawMode == DrawMode.Touch) Color.Black else Color.Gray
            )
        }
        Pencil(
            currentDrawMode = currentDrawMode,
            selectedColor = selectedColor,
            onColorButtonClicked = { openColorPickerDialog.value = true },
            onDrawModeChanged = onDrawModeChanged,
            onSizeButtonClicked = { openSizePickerDialog.value = true }
        )
        IconButton(
            onClick = {
                currentDrawMode =
                    if (currentDrawMode == DrawMode.Erase) DrawMode.Draw else DrawMode.Erase
                onDrawModeChanged(currentDrawMode)
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.eraser_icon),
                contentDescription = "Erase mode",
                modifier = Modifier.scale(0.5f),
                tint = if (currentDrawMode == DrawMode.Erase) Color.Black else Color.Gray
            )
        }
    }

    if (openSizePickerDialog.value) {
        SizePickerDialog(
            selectedSize = pencilSize,
            color = selectedColor,
            onSelected = onSizeChanged,
            onDismiss = { openSizePickerDialog.value = false }
        )
    }

    if (openColorPickerDialog.value) {
        ColorPickerDialog(
            selectedColor = selectedColor,
            onSelected = onColorChanged,
            onDismiss = { openColorPickerDialog.value = false }
        )
    }
}

@Composable
fun PaletteTopBar(
    modifier: Modifier = Modifier,
    canSave: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onSaveClicked: () -> Unit,
    unUndoClicked: () -> Unit,
    unRedoClicked: () -> Unit,
    onExportClicked: () -> Unit,
    onBroadCastUrl: (Uri) -> Unit,
    collabUrl: Uri?,
    onExportClickedAsPdf: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(MaterialTheme.shapes.large)
            .background(Color.LightGray)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onSaveClicked,
            enabled = canSave && canUndo
        ) {
            Icon(
                imageVector = Icons.Rounded.Save,
                contentDescription = "Save sketch"
            )
        }
        IconButton(
            onClick = unUndoClicked,
            enabled = canUndo
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_undo),
                contentDescription = "Undo"
            )
        }
        IconButton(
            onClick = unRedoClicked,
            enabled = canRedo
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_redo),
                contentDescription = "Redo"
            )
        }
        IconButton(onClick = { expanded = true }) {
            Icon(
                painterResource(R.drawable.ic_download),
                contentDescription = "Localized description"
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(-dropDownMenuOffset.dp, 0.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Save as PNG") },
                onClick = {
                    onExportClicked()
                    expanded = false
                },
                leadingIcon = { Icon(Icons.Outlined.Image, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Save as PDF") },
                onClick = {
                    onExportClickedAsPdf()
                    expanded = false
                },
                leadingIcon = { Icon(Icons.Outlined.PictureAsPdf, contentDescription = null) }
            )
        }
        IconButton(
            onClick = {
                collabUrl?.let {
                    onBroadCastUrl(it)
                }
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.collaboration),
                contentDescription = "Invite Collaborator",
                modifier = Modifier.size(24.dp),
                tint = Color.Black
            )
        }
    }
}

@Composable
private fun Pencil(
    currentDrawMode: DrawMode,
    selectedColor: Color,
    onColorButtonClicked: () -> Unit,
    onDrawModeChanged: (DrawMode) -> Unit,
    onSizeButtonClicked: () -> Unit
) {
    var drawMode = currentDrawMode

    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .background(Color.Gray)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                if (drawMode == DrawMode.Draw) return@IconButton
                drawMode = DrawMode.Draw
                onDrawModeChanged(drawMode)
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.Brush,
                contentDescription = "Drawing mode",
                tint = if (drawMode == DrawMode.Draw) Color.Black else Color.LightGray
            )
        }
        Box(
            Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(selectedColor)
                .border(1.dp, Color.White, CircleShape)
                .clickable { onColorButtonClicked() }
        )
        IconButton(onClick = { onSizeButtonClicked() }) {
            Icon(
                imageVector = Icons.Rounded.LineWeight,
                contentDescription = "Pencil size",
                tint = Color.Black
            )
        }
    }
}

enum class DrawMode {
    Draw, Erase, Touch
}
