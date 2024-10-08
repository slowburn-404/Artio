package dev.borisochieng.firebase.auth.data

sealed class FirebaseResponse<out T> {
    class Success<out T>(val data: T?): FirebaseResponse<T>()
    class Error(val message: String): FirebaseResponse<Nothing>()
}