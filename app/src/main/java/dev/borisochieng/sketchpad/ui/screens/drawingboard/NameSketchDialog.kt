package dev.borisochieng.sketchpad.ui.screens.drawingboard

import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.borisochieng.sketchpad.database.Sketch

@Composable
fun NameSketchDialog(
	art: Bitmap,
	onNamed: (Sketch) -> Unit,
	onDismiss: () -> Unit
) {
	var name by rememberSaveable { mutableStateOf("") }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text("Save as") },
		text = {
			OutlinedTextField(
				value = name,
				onValueChange = { name = it },
				modifier = Modifier.fillMaxWidth()
			)
		},
		dismissButton = {
			OutlinedButton(
				onClick = onDismiss,
				modifier = Modifier.fillMaxWidth(0.5f)
			) {
				Text("Cancel")
			}
		},
		confirmButton = {
			Button(
				onClick = {
					val sketch = Sketch(
						name = name,
						art = art
					)
					onNamed(sketch)
				},
				modifier = Modifier.fillMaxWidth(0.5f),
				enabled = name.isNotEmpty()
			) {
				Text("Save")
			}
		}
	)
}