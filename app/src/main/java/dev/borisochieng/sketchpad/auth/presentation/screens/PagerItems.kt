package dev.borisochieng.sketchpad.auth.presentation.screens

import androidx.annotation.DrawableRes
import dev.borisochieng.sketchpad.R

enum class PagerItems(
    @DrawableRes val imageId: Int,
    val title: String,
    val body: String
) {
    Draw(
        imageId = R.drawable.draw,
        title = "Draw on a canvas",
        body = "Bring your ideas to life"
    ),
    Collaborate(
        imageId = R.drawable.collaborate,
        title = "Collaborate",
        body = "Do not work by yourself, let other join in on the fun"
    ),
    CloudSync(
        imageId = R.drawable.cloud_sync,
        title = "Cloud sync",
        body = "Take your canvas with you no matter the device"
    )
}