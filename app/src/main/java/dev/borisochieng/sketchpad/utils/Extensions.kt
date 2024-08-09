package dev.borisochieng.sketchpad.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Extensions {

	fun Date.formatDate(): String {
		val style = SimpleDateFormat("dd/mm/yyyy", Locale.getDefault())
		return style.format(this)
	}

	fun <T> List<T>.transformList(): List<List<T>> {
		var i = 0
		val list = mutableListOf<List<T>>()
		while (i < size) {
			val tList = mutableListOf<T>()
			tList.add(this[i])
			if (i + 1 < size) { tList.add(this[i + 1]) }
			if (i + 2 < size) { tList.add(this[i + 2]) }
			if (i + 3 < size) { tList.add(this[i + 3]) }
			list.add(tList.toList())
			i += when {
				i + 3 < size -> 4
				i + 2 < size -> 3
				i + 1 < size -> 2
				else -> 1
			}
		}
		return list.toList()
	}

}