package com.neirno.restaurantas.domain.use_case.user

import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.repository.UserRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class SetUserTypeUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, role: String) = flow {
        try {
            emit(Response.Loading)

            // Обновляем роль пользователя
            emit(Response.Success(userRepository.setUserType(userId, role).await()))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }
}
