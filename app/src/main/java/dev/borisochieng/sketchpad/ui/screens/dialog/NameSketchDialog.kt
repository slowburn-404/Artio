package dev.borisochieng.sketchpad.ui.screens.dialog

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

@Composable
fun NameSketchDialog(
	onNamed: (String) -> Unit,
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
				onClick = { onNamed(name) },
				modifier = Modifier.fillMaxWidth(0.5f),
				enabled = name.isNotEmpty()
			) {
				Text("Save")
			}
		}
	)
}