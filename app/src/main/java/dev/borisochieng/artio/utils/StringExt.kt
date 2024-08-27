package dev.borisochieng.artio.utils

fun String.idFromParameter(): String {
    return this.substring(1, this.length - 1)
}