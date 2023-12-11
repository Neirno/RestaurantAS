package com.neirno.restaurantas.domain.use_case.auth

import android.util.Log
import com.neirno.restaurantas.core.constans.UserType
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.repository.AuthRepository
import com.neirno.restaurantas.domain.repository.UserRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(email: String, password: String) = flow {
        try {
            emit(Response.Loading)

            // Вход
            val authResult = authRepository.signIn(email, password).await()

            val uuid = authResult.user?.uid
                ?: throw IllegalStateException("User ID not found")


            // Роль
            val userRoleSnapshot = userRepository.getUserInfo(uuid).await()
            val userRoleString = userRoleSnapshot?.getString("userType") ?: ""
            val userRole = UserType.fromString(userRoleString)
            emit(Response.Success(Pair(authResult, userRole)))
        } catch (e: Exception) {
            Log.i("Auth", e.localizedMessage ?: "Unexpected error.")

            if (e is CancellationException) throw e
            emit(Response.Error(e.localizedMessage ?: "Unexpected error."))
        }
    }
}