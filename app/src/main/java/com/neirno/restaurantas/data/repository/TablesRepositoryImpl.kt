package com.neirno.restaurantas.data.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import com.neirno.restaurantas.domain.model.TableModel
import com.neirno.restaurantas.domain.repository.TablesRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class TablesRepositoryImpl (
    private val firestore: FirebaseFirestore
): TablesRepository {

    private val tablesCollection = firestore.collection("tables")

    override suspend fun getTables(): Flow<List<TableModel>> = callbackFlow {
        firestore.clearPersistence()

        Log.i("getTables in reposytory", "start")
        val listener = tablesCollection.addSnapshotListener{ querySnapshot, error ->
            if (error != null) {
                Log.e("getTables in reposytory", "Error")
                close(error) // Закрыть поток с ошибкой
                return@addSnapshotListener
            }

            Log.i("getTables in reposytory", "try get tables")
            val tables = querySnapshot?.documents?.mapNotNull { document ->
                document.toObject(TableModel::class.java)?.copy(tableId = document.id)
            } ?: emptyList()

            Log.i("getTables in reposytory", "trySend in next")
            // Когда поток заканчивается или отменяется, удаляем слушателя
            trySend(tables).getOrThrow()
        }

        awaitClose { listener.remove() }
    }

    override suspend fun serviceTable(tableId: String, uuid: String): Task<Void> {
        return tablesCollection.document(tableId).update(
            "servedBy", uuid
        )
    }

    override suspend fun getTableInfo(tableId: String): Task<DocumentSnapshot?> {
        return tablesCollection.document(tableId).get()
    }

    override suspend fun reserveTable(tableId: String, persons: List<String>): Task<Void> {
        return tablesCollection.document(tableId).update(
            "reserved", true,
            "reservedBy", persons,
        )
    }

    override suspend fun freeTable(tableId: String): Task<Void> {
        return tablesCollection.document(tableId).update(
            "reserved", false,
            "reservedBy", emptyList<String>(), // Пустой список для снятия резервирования,
            "servedBy", ""
        )
    }
}