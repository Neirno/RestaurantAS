package com.neirno.restaurantas.domain.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.neirno.restaurantas.domain.model.UserModel
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun setUserType(userId: String, role: String): Task<Void>

    suspend fun setUserStatus(userId: String, status: Boolean): Task<Void>

    suspend fun getUserInfo(userId: String): Task<DocumentSnapshot?>

    suspend fun getUserInfoFlow(userId: String): Flow<UserModel>

    suspend fun getUserId(): String?

    suspend fun getUsersFlow(): Flow<List<UserModel>>
}