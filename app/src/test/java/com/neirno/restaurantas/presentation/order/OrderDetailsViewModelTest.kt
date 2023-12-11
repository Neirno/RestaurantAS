package com.neirno.restaurantas.presentation.order

import androidx.lifecycle.SavedStateHandle
import com.neirno.restaurantas.core.constans.OrderStatus
import com.neirno.restaurantas.core.constans.UserType
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.model.OrderModel
import com.neirno.restaurantas.domain.use_case.order.AcceptOrderUseCase
import com.neirno.restaurantas.domain.use_case.order.CancelOrderUseCase
import com.neirno.restaurantas.domain.use_case.order.CreateOrderUseCase
import com.neirno.restaurantas.domain.use_case.order.GetOrderByIdUseCase
import com.neirno.restaurantas.domain.use_case.order.GetOrdersUseCase
import com.neirno.restaurantas.domain.use_case.order.SetNextOrderStatusUseCase
import com.neirno.restaurantas.domain.use_case.table.FreeTableUseCase
import com.neirno.restaurantas.domain.use_case.table.GetTableInfoUseCase
import com.neirno.restaurantas.domain.use_case.user.GetUserUIdUseCase
import com.neirno.restaurantas.presentation.ui.order_details.OrderDetailsEvent
import com.neirno.restaurantas.presentation.ui.order_details.OrderDetailsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class OrderDetailsViewModelTest {

    private lateinit var viewModel: OrderDetailsViewModel
    private val getOrderByIdUseCase: GetOrderByIdUseCase = mockk()
    private val createOrderUseCase: CreateOrderUseCase = mockk()
    private val acceptOrderUseCase: AcceptOrderUseCase = mockk()
    private val setNextOrderStatusUseCase: SetNextOrderStatusUseCase = mockk()
    private val cancelOrderUseCase: CancelOrderUseCase = mockk()
    private val freeTableUseCase: FreeTableUseCase = mockk()
    private val getUserUIdUseCase: GetUserUIdUseCase = mockk()
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val testCoroutineDispatcher = TestCoroutineDispatcher()


    @Before
    fun setup() {
        Dispatchers.setMain(testCoroutineDispatcher)

        // Настройка начального состояния
        every { savedStateHandle.get<String>("orderId") } returns "mockOrderId"
        every { savedStateHandle.get<String>("tableNumber") } returns "mockTableNumber"
        every { savedStateHandle.get<String>("tableId") } returns "mockTableId"
        every { savedStateHandle.get<String>("userType") } returns UserType.WAITER.type

        // Мокирование getOrderUseCase
        val initialOrder = OrderModel(status = OrderStatus.UNKNOWN.status, tableId = "mockTableId", note = "AA")
        val createdOrder = initialOrder.copy(status = OrderStatus.PENDING.status) // Представляет собой заказ после создания
        coEvery { getOrderByIdUseCase("mockOrderId") } returnsMany listOf(
            flowOf(Response.Success(initialOrder)),
            flowOf(Response.Success(createdOrder))
        )

        // Мокирование createOrderUseCase
        coEvery { createOrderUseCase(any()) } returns flowOf(Response.Success(mockk()))
        // Мокирование cancelOrderUseCase
        coEvery { cancelOrderUseCase(any()) } returns flowOf(Response.Success(mockk()))

        viewModel = OrderDetailsViewModel(
            getOrderByIdUseCase,
            createOrderUseCase,
            acceptOrderUseCase,
            setNextOrderStatusUseCase,
            cancelOrderUseCase,
            freeTableUseCase,
            getUserUIdUseCase,
            savedStateHandle
        )
        testCoroutineDispatcher.scheduler.advanceUntilIdle()
        coVerify { savedStateHandle.get<String>("userType") }

    }

    @Test
    fun `when waiter creates and cancels an order, check state changes`() = runBlocking {
        // Загрузка начального состояния заказа
        viewModel.onEvent(OrderDetailsEvent.LoadOrderDetails)
        testCoroutineDispatcher.scheduler.advanceUntilIdle()

        // Проверка начального состояния заказа
        assertTrue(viewModel.container.stateFlow.value.order.status == OrderStatus.UNKNOWN.status)

        // Создание заказа
        viewModel.onEvent(OrderDetailsEvent.ChangeOrderStatusDetails)
        testCoroutineDispatcher.scheduler.advanceUntilIdle()

        // Заново прогружаем
        viewModel.onEvent(OrderDetailsEvent.LoadOrderDetails)
        testCoroutineDispatcher.scheduler.advanceUntilIdle()

        // Проверка состояния после создания заказа
        assertTrue(viewModel.container.stateFlow.value.order.status == OrderStatus.PENDING.status)
        assertTrue(viewModel.container.stateFlow.value.setCancelOrder)

        // Отмена заказа
        viewModel.onEvent(OrderDetailsEvent.CancelOrderDetails)
        testCoroutineDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `when waiter loads an order with UNKNOWN status, canEditNote should be true`() = runBlocking {
        // Дано: тип пользователя - официант, и заказ со статусом UNKNOWN
        every { savedStateHandle.get<String>("userType") } returns UserType.WAITER.type
        val initialOrder = OrderModel(status = OrderStatus.UNKNOWN.status, tableId = "mockTableId", note = "Initial Note")
        coEvery { getOrderByIdUseCase("mockOrderId") } returns flowOf(Response.Success(initialOrder))

        // Когда: загружаются детали заказа
        viewModel.onEvent(OrderDetailsEvent.LoadOrderDetails)
        testCoroutineDispatcher.scheduler.advanceUntilIdle()

        // Тогда: canEditNote должен быть true для официанта с заказом со статусом UNKNOWN
        val state = viewModel.container.stateFlow.value
        assertTrue(state.canEditNote)
    }

    @Test
    fun `when cook loads an order with IN_PROGRESS status, canEditNote should be false`() = runBlocking {
        // Дано: тип пользователя - повар, и заказ со статусом IN_PROGRESS
        every { savedStateHandle.get<String>("userType") } returns UserType.COOK.type
        val inProgressOrder = OrderModel(status = OrderStatus.IN_PROGRESS.status, tableId = "mockTableId", note = "In Progress Note")
        coEvery { getOrderByIdUseCase("mockOrderId") } returns flowOf(Response.Success(inProgressOrder))

        // Когда: загружаются детали заказа
        viewModel.onEvent(OrderDetailsEvent.LoadOrderDetails)
        testCoroutineDispatcher.scheduler.advanceUntilIdle()

        // Тогда: canEditNote должен быть false для повара с заказом со статусом IN_PROGRESS
        val state = viewModel.container.stateFlow.value
        assertFalse(!state.canEditNote)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testCoroutineDispatcher.cleanupTestCoroutines()
    }
}
