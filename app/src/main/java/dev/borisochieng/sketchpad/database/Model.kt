package dev.borisochieng.sketchpad.database

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date

@Entity
data class Sketch(
	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,
	val name: String,
	val dateCreated: Date = Calendar.getInstance().time,
	val lastModified: Date = Calendar.getInstance().time,
	val paths: List<PathProperties>
)

data class PathProperties(
	var alpha: Float = 1f,
	var color: Color = Color.Black,
	var eraseMode: Boolean = false,
	val start: Offset = Offset.Zero,
	val end: Offset = Offset.Zero,
	var strokeCap: StrokeCap = StrokeCap.Round,
	var strokeJoin: StrokeJoin = StrokeJoin.Round,
	var strokeWidth: Float = 10f
)