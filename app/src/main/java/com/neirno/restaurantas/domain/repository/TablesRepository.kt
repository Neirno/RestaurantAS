package com.neirno.restaurantas.domain.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.neirno.restaurantas.domain.model.TableModel
import kotlinx.coroutines.flow.Flow

interface TablesRepository {
    suspend fun getTables(): Flow<List<TableModel>>

    suspend fun reserveTable(tableId: String, persons: List<String>): Task<Void>

    suspend fun freeTable(tableId: String): Task<Void>

    suspend fun getTableInfo(tableId: String): Task<DocumentSnapshot?> //Task<DocumentSnapshot?>

    suspend fun serviceTable(tableId: String, uuid: String): Task<Void>
}