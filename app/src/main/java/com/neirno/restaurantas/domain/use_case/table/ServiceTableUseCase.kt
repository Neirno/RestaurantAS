package com.neirno.restaurantas.domain.use_case.table

import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.repository.TablesRepository
import com.neirno.restaurantas.domain.repository.UserRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class ServiceTableUseCase @Inject constructor(
    private val tablesRepository: TablesRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(tableId: String) = flow {
        try {
            emit(Response.Loading)

            val uuid = userRepository.getUserId()

            if (uuid == null)
                emit(Response.Error("Произошла ошибка!"))
            else
                emit(Response.Success(tablesRepository.serviceTable(tableId, uuid).await()))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }
}