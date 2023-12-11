import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.neirno.restaurantas.core.constans.OrderStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.neirno.restaurantas.domain.model.OrderModel
import com.neirno.restaurantas.domain.repository.OrdersRepository

class OrdersRepositoryImpl(
    private val firestore: FirebaseFirestore
) : OrdersRepository {

    private val orderCollection = firestore.collection("orders")

    override suspend fun getOrdersFlow(): Flow<List<OrderModel>?> = callbackFlow {
        val listener = orderCollection.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val orders = querySnapshot?.documents
                ?.mapNotNull { it.toObject(OrderModel::class.java)?.copy(orderId = it.id) }

            trySend(orders).getOrThrow()
        }
        awaitClose{ listener.remove() }
    }

    override suspend fun getOrderById(orderId: String): Flow<OrderModel?> = callbackFlow {
        val listener = orderCollection.document(orderId).addSnapshotListener { query, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val order = query?.toObject(OrderModel::class.java)?.copy(orderId = orderId)

            trySend(order).getOrThrow()
        }
        awaitClose{ listener.remove() }
    }

    override suspend fun acceptOrderUseCase(
        orderId: String,
        newStatus: OrderStatus,
        takenBy: String
    ): Task<Void> {
        return orderCollection
            .document(orderId)
            .update(mapOf(
            "status" to newStatus.status,
            "takenBy" to takenBy
            )
        )
    }

    override suspend fun cancelOrder(orderId: String): Task<Void> {
        return orderCollection.document(orderId)
            .update("status", OrderStatus.CANCELLED.status)
    }

    override suspend fun setNextOrderStatusUseCase(
        orderId: String,
        newStatus: OrderStatus
    ): Task<Void> {
        return orderCollection
            .document(orderId)
            .update(mapOf(
                "status" to newStatus.status,
            )
        )
    }


    override suspend fun createOrder(order: OrderModel): Task<DocumentReference?> {
        return firestore.collection("orders").add(order)
    }
}
