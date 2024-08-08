package dev.borisochieng.sketchpad.utils

import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.borisochieng.sketchpad.database.Path
import io.ak1.drawbox.PathWrapper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Extensions {

	fun Date.formatDate(): String {
		val style = SimpleDateFormat("dd/mm/yyyy", Locale.getDefault())
		return style.format(this)
	}

	fun PathWrapper.toPath() = Path(
		points = points.toList().toJsonString(),
		strokeWidth = strokeWidth,
		strokeColor = strokeColor.toArgb(),
		alpha = alpha
	)

	fun Path.toPathWrapper() = PathWrapper(
		points = points.toPoints().toMutableStateList(),
		strokeWidth = strokeWidth,
		strokeColor = Color(strokeColor),
		alpha = alpha
	)

	private fun List<Offset>.toJsonString(): String {
		val gson = Gson()
		return gson.toJson(this)
	}

	private fun String.toPoints(): List<Offset> {
		val gson = Gson()
		val type = object : TypeToken<List<Offset>>() {}.type
		return gson.fromJson(this, type)
	}

}