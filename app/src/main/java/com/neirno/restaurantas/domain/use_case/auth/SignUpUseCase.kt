package com.neirno.restaurantas.domain.use_case.auth

import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.model.UserModel
import com.neirno.restaurantas.domain.repository.AuthRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        email: String,
        password: String,
        user: UserModel
    ) = flow {
        try {
            emit(Response.Loading)
            emit(Response.Success(authRepository.signUp(email, password, user).await()))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.localizedMessage ?: "Unexpected error."))
        }
    }
}