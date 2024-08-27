package dev.borisochieng.artio.ui.screens.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

// Dialog showing a prompt to save new sketch or changes before navigating back
@Composable
fun SavePromptDialog(
	sketchIsNew: Boolean,
	onSave: () -> Unit,
	onDiscard: () -> Unit,
	onDismiss: () -> Unit
) {
	val status = if (sketchIsNew) "sketch" else "changes"

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text("Discard $status?") },
		text = {
			Text("${status.capFirstLetter()} not saved. Are you sure you want to discard $status?")
		},
		confirmButton = {
			Button(onClick = { onSave(); onDismiss() }) {
				Text("Save")
			}
		},
		dismissButton = {
			OutlinedButton(onClick = { onDiscard(); onDismiss() }) {
				Text("Discard")
			}
		}
	)
}

private fun String.capFirstLetter(): String {
	return substring(0, 1).uppercase() + substring(1)
}