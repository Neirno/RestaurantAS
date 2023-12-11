package com.neirno.restaurantas.domain.use_case.table

import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.repository.TablesRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class FreeTableUseCase @Inject constructor(
    private val tablesRepository: TablesRepository
) {
    suspend operator fun invoke(tableId: String) = flow {
        try {
            emit(Response.Loading)

            // Обновляем роль пользователя
            emit(Response.Success(tablesRepository.freeTable(tableId).await()))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }
}
