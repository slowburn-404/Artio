package dev.borisochieng.sketchpad.database

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.borisochieng.sketchpad.ui.screens.drawingboard.alt.PathProperties
import java.util.Calendar
import java.util.Date

@Entity
data class Sketch(
	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,
	val name: String,
	val dateCreated: Date = Calendar.getInstance().time,
	val lastModified: Date = Calendar.getInstance().time,
	val pathList: List<PathProperties>
)

data class Path(
	var points: String, // list of Offsets saved as String
	val strokeWidth: Float,
	val strokeColor: Int,
	val alpha: Float
)
