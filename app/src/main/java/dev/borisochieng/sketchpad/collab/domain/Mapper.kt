package dev.borisochieng.sketchpad.collab.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import dev.borisochieng.sketchpad.collab.data.models.DBPathProperties
import dev.borisochieng.sketchpad.collab.data.models.DBSketch
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.ui.screens.drawingboard.alt.PathProperties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun DBSketch.toSketch(): Sketch {
    return Sketch(
        id = id,
        name = title,
        pathList = paths.map { it.toPathProperties() },
        dateCreated = dateConverter(dateCreated)!!,
        lastModified = dateConverter(lastModified)!!
    )

}

fun DBPathProperties.toPathProperties(): PathProperties {
    return PathProperties(
        alpha = alpha.toFloat(),
        color = hexToColor(color),
        eraseMode = eraseMode,
        start = Offset(start.x.toFloat(), start.y.toFloat()),
        end = Offset(end.x.toFloat(), end.y.toFloat()),
        strokeWidth = strokeWidth.toFloat()
    )
}

private fun hexToColor(hex: String): Color {
    val cleanHex = if(hex.startsWith("#")) hex.substring(1) else hex
    val colorInt = Integer.parseInt(cleanHex, 16)

    return Color(colorInt)
}

private fun dateConverter(dateString: String?): Date? {
    val formatter = "yyyy-MM-dd"
    return dateString?.let{
        val dateFormat = SimpleDateFormat(formatter, Locale.getDefault())
        dateFormat.parse(it)
    }
}