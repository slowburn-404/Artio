package dev.borisochieng.sketchpad.database

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
