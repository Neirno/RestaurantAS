package com.neirno.restaurantas.domain.use_case.user

import kotlinx.coroutines.flow.flow
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.repository.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.catch

class GetUserStatusUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: String? = null) = flow {
        try {
            emit(Response.Loading)

            val uuid = userId ?: userRepository.getUserId()
            ?: throw IllegalStateException("User ID not found")

            userRepository.getUserInfoFlow(uuid)
                .catch { e ->
                    emit(Response.Error("Статус пользователя не получен: ${e.localizedMessage}"))
                }
                .collect { user ->
                    emit(Response.Success(user.working))
                }
        } catch (e: Exception) {
            emit(Response.Error(e.localizedMessage ?: "Неожиданная ошибка."))
        }
    }
}
