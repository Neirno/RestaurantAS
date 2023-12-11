package com.neirno.restaurantas.domain.use_case.user

import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.repository.UserRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class GetUserUIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): String? {
        return userRepository.getUserId()
    }
}
