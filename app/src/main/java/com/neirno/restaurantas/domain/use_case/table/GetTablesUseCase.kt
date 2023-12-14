package com.neirno.restaurantas.domain.use_case.table

import android.util.Log
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.model.TableModel
import com.neirno.restaurantas.domain.repository.TablesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.util.concurrent.CancellationException
import javax.inject.Inject

class GetTablesUseCase @Inject constructor(
    private val tablesRepository: TablesRepository
) {
    suspend operator fun invoke(): Flow<Response<List<TableModel>>> = flow {
        try {
            emit(Response.Loading)

            tablesRepository.getTables()
                .map { tables ->
                    Response.Success(tables) // Преобразуем каждое успешное значение в обертку Response
                }
                .catch { e ->
                    emit(
                        Response.Error(
                            e.localizedMessage ?: "Unexpected error."
                        )
                    ) // Перехватываем исключения и отправляем ошибку
                }
                .collect { response ->
                    emit(response) // Эмитим каждое значение из потока
                }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(
                Response.Error(
                    e.localizedMessage ?: "Unexpected error."
                )
            ) // Если возникло исключение вне потока, перехватываем его здесь
        }
    }
}