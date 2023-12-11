package com.neirno.restaurantas.domain.use_case.user

import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.model.UserModel
import com.neirno.restaurantas.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.CancellationException
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Flow<Response<List<UserModel>>> = flow {
        try {
            emit(Response.Loading)

            userRepository.getUsersFlow()
                .map { users ->
                    Response.Success(users)
                }
                .collect { response ->
                    emit(response)
                }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.localizedMessage ?: "Unexpected error.")) // Если возникло исключение вне потока, перехватываем его здесь
        }
    }
}