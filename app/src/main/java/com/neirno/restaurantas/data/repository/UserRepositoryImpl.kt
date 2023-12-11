package com.neirno.restaurantas.data.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.toObject
import com.neirno.restaurantas.domain.model.UserModel
import com.neirno.restaurantas.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class UserRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {
    override suspend fun setUserType(userId: String, role: String): Task<Void> {
        return firestore.collection("users")
            .document(userId)
            .update("userType", role)

    }

    override suspend fun setUserStatus(userId: String, status: Boolean): Task<Void> {
        return firestore.collection("users")
            .document(userId)
            .update("working", status)

    }

    override suspend fun getUserInfo(userId: String): Task<DocumentSnapshot?> {
        return firestore.collection("users")
            .document(userId)
            .get()
    }

    override suspend fun getUserInfoFlow(userId: String): Flow<UserModel> = callbackFlow {
        val documentReference = firestore.collection("users").document(userId)

        // Создаем слушатель изменений документа
        val listener = documentReference.addSnapshotListener { documentSnapshot, error ->

            if (error != null) {
                // Обработка ошибки
                close(error) // Закрыть поток с ошибкой
                return@addSnapshotListener
            }
            val user = documentSnapshot?.toObject(UserModel::class.java)?: UserModel()
            Log.i("GetUserStatusUseCase",user.toString())

            trySend(user).getOrThrow()
        }

        awaitClose { listener.remove() }
    }

    override suspend fun getUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override suspend fun getUsersFlow(): Flow<List<UserModel>> = callbackFlow {
        val userCollection = firestore.collection("users")
        val listener = userCollection.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val users = querySnapshot?.documents
                ?.mapNotNull { it.toObject(UserModel::class.java) }?: emptyList()

            trySend(users).getOrThrow()
        }
        awaitClose{ listener.remove() }
    }
}