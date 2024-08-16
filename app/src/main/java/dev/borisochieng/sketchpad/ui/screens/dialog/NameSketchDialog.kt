package dev.borisochieng.sketchpad.ui.screens.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardCapitalization

@Composable
fun NameSketchDialog(
	currentName: String = "",
	onNamed: (String) -> Unit,
	onDismiss: () -> Unit
) {
	var name by rememberSaveable { mutableStateOf(currentName) }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text("Save as") },
		text = {
			OutlinedTextField(
				value = name,
				onValueChange = { name = it },
				modifier = Modifier.fillMaxWidth(),
				keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
				singleLine = true
			)
		},
		confirmButton = {
			Button(
				onClick = { onNamed(name) },
				enabled = name.isNotEmpty() && name != currentName
			) {
				Text("Save")
			}
		},
		dismissButton = {
			OutlinedButton(onClick = onDismiss) {
				Text("Cancel")
			}
		}
	)
}