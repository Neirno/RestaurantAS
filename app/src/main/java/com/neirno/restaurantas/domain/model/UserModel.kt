package com.neirno.restaurantas.domain.model

import com.neirno.restaurantas.core.constans.UserType

data class UserModel(
    val userId: String = "",
    val username: String = "",
    val googleAuth: Boolean = false,
    val userType: String = UserType.UNKNOWN.type,
    val working: Boolean = false,
)
