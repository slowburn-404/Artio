package dev.borisochieng.sketchpad.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
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
	fun fromBitmap(bitmap: Bitmap): ByteArray {
		val stream = ByteArrayOutputStream()
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
		return stream.toByteArray()
	}

	@TypeConverter
	fun toBitmap(byteArray: ByteArray): Bitmap {
		val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
		return bitmap
	}

	@TypeConverter
	fun fromColor(color: Color) = color.toArgb()

	@TypeConverter
	fun toColor(value: Int) = Color(value)

	@TypeConverter
	fun fromPaths(paths: List<Path>): String {
		val gson = Gson()
		return gson.toJson(paths)
	}

	@TypeConverter
	fun toPaths(pathJson: String): List<Path> {
		val gson = Gson()
		val type = object : TypeToken<List<Path>>() {}.type
		return gson.fromJson(pathJson, type)
	}

}