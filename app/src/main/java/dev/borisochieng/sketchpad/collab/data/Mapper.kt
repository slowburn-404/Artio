package dev.borisochieng.sketchpad.collab.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.borisochieng.sketchpad.collab.data.models.DBOffset
import dev.borisochieng.sketchpad.collab.data.models.DBPathProperties
import dev.borisochieng.sketchpad.collab.data.models.DBSketch
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.ui.screens.drawingboard.alt.PathProperties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun PathProperties.toDBPathProperties(): DBPathProperties {
    return DBPathProperties(
        alpha = alpha.toDouble(),
        color = color.toHexString(),
        eraseMode = eraseMode,
        start = DBOffset(x = start.x.toDouble(), y = start.y.toDouble()),
        end = DBOffset(x = end.x.toDouble(), y= end.y.toDouble() ),
        strokeWidth = strokeWidth.toDouble()
    )
}

fun Sketch.toDBSketch(): DBSketch {
    return DBSketch(
        id = id,
        dateCreated = dateConverter(dateCreated),
        lastModified = dateConverter(lastModified),
        title = name,
        paths = pathList.map { it.toDBPathProperties() },
    )
}


private fun Color.toHexString(): String {
    val argb = this.toArgb()
    return String.format("#%08x", argb)
}

private fun dateConverter(date: Date): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(date)
}