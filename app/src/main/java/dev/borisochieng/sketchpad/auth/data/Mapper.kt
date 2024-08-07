package dev.borisochieng.sketchpad.auth.data

import dev.borisochieng.sketchpad.auth.data.model.UserCredentials
import dev.borisochieng.sketchpad.auth.domain.model.User

fun UserCredentials.toDomainUser(): User =
    User(
       email = email
    )