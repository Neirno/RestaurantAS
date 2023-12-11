package com.neirno.restaurantas.domain.use_case.user

import android.util.Log
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.repository.UserRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class SetUserStatusUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, status: Boolean) = flow {
        try {
            emit(Response.Loading)
            // Обновляем роль пользователя
            emit(Response.Success(userRepository.setUserStatus(userId, status).await()))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }
}
