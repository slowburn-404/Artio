package dev.borisochieng.sketchpad.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.PathProperties
import java.util.Date

class TypeConverter {

	@TypeConverter
	fun fromDate(date: Date?) = date?.time

	@TypeConverter
	fun toDate(millisSinceEpoch: Long?): Date? {
		return millisSinceEpoch?.let {
			Date(it)
		}
	}

	@TypeConverter
	fun fromPaths(paths: List<PathProperties>): String {
		val gson = Gson()
		return gson.toJson(paths)
	}

	@TypeConverter
	fun toPaths(pathJson: String): List<PathProperties> {
		val gson = Gson()
		val type = object : TypeToken<List<PathProperties>>() {}.type
		return gson.fromJson(pathJson, type)
	}

}