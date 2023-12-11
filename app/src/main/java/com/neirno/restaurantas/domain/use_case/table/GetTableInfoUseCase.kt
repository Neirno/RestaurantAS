package com.neirno.restaurantas.domain.use_case.table

import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.model.TableModel
import com.neirno.restaurantas.domain.repository.TablesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class GetTableInfoUseCase @Inject constructor(
    private val tablesRepository: TablesRepository
) {
    suspend operator fun invoke(tableId: String): Flow<Response<TableModel>> = flow {
        try {
            emit(Response.Loading)

            val documentSnapshot = tablesRepository.getTableInfo(tableId).await()
            val tableInfo: TableModel? = documentSnapshot?.toObject(TableModel::class.java)?.copy(tableId = documentSnapshot.id)

            if (tableInfo != null)
                emit(Response.Success(tableInfo))
            else
                emit(Response.Error("Информация о столе недоступна"))

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }
}
/*    suspend operator fun invoke(tableId: String): Flow<Response<TableModel>> = flow {
        try {
            emit(Response.Loading)

            val documentSnapshot = tablesRepository.getTableInfo(tableId).await()
            val tableInfo: TableModel? = documentSnapshot?.toObject(TableModel::class.java)?.copy(tableId = documentSnapshot.id)

            if (tableInfo != null) {
                emit(Response.Success(tableInfo))
            } else {
                emit(Response.Error("Table information not found."))
            }
        } catch (e: Exception) {
            emit(Response.Error(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }

 */