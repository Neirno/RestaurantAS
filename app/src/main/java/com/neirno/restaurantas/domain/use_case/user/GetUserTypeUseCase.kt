package com.neirno.restaurantas.domain.use_case.user

import com.google.firebase.firestore.DocumentSnapshot
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.repository.UserRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class GetUserTypeUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: String? = null) = flow {
        try {
            emit(Response.Loading)

            val uuid = userId ?: userRepository.getUserId()
            ?: throw IllegalStateException("User ID not found")

            userRepository.getUserInfoFlow(uuid)
                .catch {
                    emit(Response.Error("Тип пользователя не получен."))
                }
                .collect { user ->
                    emit(Response.Success(user.userType))
                }

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.localizedMessage ?: "Unexpected error."))
        }
    }
}