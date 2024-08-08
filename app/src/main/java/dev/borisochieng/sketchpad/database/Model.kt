package dev.borisochieng.sketchpad.database

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date

@Entity
data class Sketch(
	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,
	val name: String = "",
	val dateCreated: Date = Calendar.getInstance().time,
	val lastModified: Date = Calendar.getInstance().time,
	val art: Bitmap
)
