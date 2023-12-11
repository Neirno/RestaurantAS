package com.neirno.restaurantas.data.repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.model.UserModel
import com.neirno.restaurantas.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Task<AuthResult> {
        return firebaseAuth.signInWithEmailAndPassword(email, password)
    }

    override suspend fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun signUp(email: String, password: String, user: UserModel): Task<AuthResult> {
        return firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val firestoreUser = hashMapOf(
                    "userId" to user.userId,
                    "username" to user.username,
                    "userType" to user.userType
                )
                firestore.collection("users").document(authResult.user?.uid!!)
                    .set(firestoreUser)
            }
    }
}