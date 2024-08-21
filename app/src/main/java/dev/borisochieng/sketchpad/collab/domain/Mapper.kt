package dev.borisochieng.sketchpad.collab.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import dev.borisochieng.sketchpad.collab.data.models.DBPathProperties
import dev.borisochieng.sketchpad.collab.data.models.DBSketch
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.ui.screens.drawingboard.alt.PathProperties
import dev.borisochieng.sketchpad.utils.DATE_PATTERN
import dev.borisochieng.sketchpad.utils.Extensions.toColor
import dev.borisochieng.sketchpad.utils.Extensions.toDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun DBSketch.toSketch(): Sketch {
    return Sketch(
        id = id,
        name = title,
        pathList = paths.map { it.toPathProperties() },
        dateCreated = dateCreated.toDate()!!,
        lastModified = lastModified.toDate()!!
    )

}

fun DBPathProperties.toPathProperties(): PathProperties {
    return PathProperties(
        id = id,
        alpha = alpha,
        color = color.toColor(),
        eraseMode = eraseMode,
        start = Offset(start.x, start.y),
        end = Offset(end.x, end.y),
        strokeWidth = strokeWidth
    )
}