package dev.borisochieng.sketchpad.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Extensions {

	fun Date.formatDate(): String {
		val style = SimpleDateFormat("dd/mm/yyyy", Locale.getDefault())
		return style.format(this)
	}

}