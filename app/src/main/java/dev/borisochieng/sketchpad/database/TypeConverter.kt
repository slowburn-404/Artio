package dev.borisochieng.sketchpad.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class TypeConverter {

	@TypeConverter
	fun fromDate(date: Date?): Long? {
		return date?.time
	}

	@TypeConverter
	fun toDate(millisSinceEpoch: Long?): Date? {
		return millisSinceEpoch?.let {
			Date(it)
		}
	}

	@TypeConverter
	fun fromPathProperties(paths: List<PathProperties>): String {
		val gson = Gson()
		return gson.toJson(paths)
	}

	@TypeConverter
	fun toPathProperties(pathJson: String): List<PathProperties> {
		val gson = Gson()
		val type = object : TypeToken<List<PathProperties>>() {}.type
		return gson.fromJson(pathJson, type)
	}

}