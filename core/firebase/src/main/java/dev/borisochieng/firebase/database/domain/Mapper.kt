package dev.borisochieng.firebase.database.domain

import androidx.compose.ui.geometry.Offset
import dev.borisochieng.artio.database.Sketch
import dev.borisochieng.artio.ui.screens.drawingboard.data.PathProperties
import dev.borisochieng.artio.utils.Extensions.toColor
import dev.borisochieng.artio.utils.Extensions.toDate

fun dev.borisochieng.firebase.model.SketchFirebase.toSketch(): Sketch {
    return Sketch(
        id = id,
        name = title,
        pathList = paths.map { it.toPathProperties() },
        dateCreated = dateCreated.toDate()!!,
        lastModified = lastModified.toDate()!!,
        textList = emptyList(),
        isBackedUp = isBackedUp
    )

}

fun dev.borisochieng.firebase.model.PathPropertiesFirebase.toPathProperties(): PathProperties {
    return PathProperties(
        id = id,
        alpha = alpha,
        color = color.toColor(),
        textMode = eraseMode,
        start = Offset(start.x, start.y),
        end = Offset(end.x, end.y),
        strokeWidth = strokeWidth
    )
}