package com.neirno.restaurantas.domain.use_case.auth

import com.neirno.restaurantas.core.constans.UserType
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.repository.AuthRepository
import com.neirno.restaurantas.domain.repository.UserRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class IsLoggedInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: String? = null) = flow {
        try {
            emit(Response.Loading)

            val isLoggedIn = authRepository.isLoggedIn()
            if (!isLoggedIn) {
                emit(Response.Error("User is not logged in"))
                return@flow
            }

            val uuid = userId ?: userRepository.getUserId()
            ?: throw IllegalStateException("User ID not found")

            // Роль
            val userRoleSnapshot = userRepository.getUserInfo(uuid).await()
            val userRoleString = userRoleSnapshot?.getString("userType") ?: ""
            val userRole = UserType.fromString(userRoleString)
            //val userRole = userRoleString

            emit(Response.Success(userRole))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.localizedMessage ?: "Unexpected error."))
        }
    }
}
