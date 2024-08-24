package dev.borisochieng.sketchpad.utils

fun String.idFromParameter(): String {
    return this.substring(1, this.length - 1)
}