package dev.borisochieng.firebase.database.data

import dev.borisochieng.artio.database.Sketch
import dev.borisochieng.artio.ui.screens.drawingboard.data.PathProperties
import dev.borisochieng.artio.utils.Extensions.formatDate
import dev.borisochieng.artio.utils.Extensions.toHexString
import java.util.UUID.randomUUID

fun PathProperties.toDBPathProperties(): dev.borisochieng.firebase.model.PathPropertiesFirebase {
    return DBPathProperties(
        id = id.takeIf { it.isNotEmpty() } ?: randomUUID().toString(),
        alpha = alpha,
        color = color.toHexString(),
        eraseMode = textMode,
        start = DBOffset(x = start.x, y = start.y),
        end = DBOffset(x = end.x, y= end.y ),
        strokeWidth = strokeWidth
    )
}

fun Sketch.toDBSketch(): dev.borisochieng.firebase.model.SketchFirebase {
    return DBSketch(
        id = id,
        dateCreated = dateCreated.formatDate(),
        lastModified = lastModified.formatDate(),
        title = name,
        paths = pathList.map { it.toDBPathProperties() },
        isBackedUp = isBackedUp
    )
}