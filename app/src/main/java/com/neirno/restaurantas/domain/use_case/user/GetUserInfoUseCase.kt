package com.neirno.restaurantas.domain.use_case.user

import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.model.UserModel
import com.neirno.restaurantas.domain.repository.UserRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String? = null) = flow {
        try {
            emit(Response.Loading)

            val uuid = userId ?: userRepository.getUserId()
            ?: throw IllegalStateException("User ID not found")

            val documentSnapshot = userRepository.getUserInfo(uuid).await()
            val user = documentSnapshot?.toObject(UserModel::class.java)?: UserModel()

            emit(Response.Success(user))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.localizedMessage ?: "Unexpected error.")) // Если возникло исключение вне потока, перехватываем его здесь

        }
    }
}