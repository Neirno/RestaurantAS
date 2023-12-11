package com.neirno.restaurantas.domain.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.QuerySnapshot
import com.neirno.restaurantas.domain.model.UserModel
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Task<AuthResult>

    suspend fun isLoggedIn(): Boolean

    suspend fun signUp(email: String, password: String, user: UserModel): Task<AuthResult>

    suspend fun signOut()
}