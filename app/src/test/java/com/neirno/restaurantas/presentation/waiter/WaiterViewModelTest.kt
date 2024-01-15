package com.neirno.restaurantas.presentation.waiter

import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.model.TableModel
import com.neirno.restaurantas.domain.use_case.services.StartOrderStatusServiceUseCase
import com.neirno.restaurantas.domain.use_case.table.GetTablesUseCase
import com.neirno.restaurantas.domain.use_case.table.ServiceTableUseCase
import com.neirno.restaurantas.domain.use_case.user.GetUserStatusUseCase
import com.neirno.restaurantas.domain.use_case.user.GetUserTypeUseCase
import com.neirno.restaurantas.domain.use_case.user.GetUserUIdUseCase
import com.neirno.restaurantas.presentation.ui.waiter.WaiterEvent
import com.neirno.restaurantas.presentation.ui.waiter.WaiterViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class WaiterViewModelTest {

    private lateinit var viewModel: WaiterViewModel
    private val getTablesUseCase: GetTablesUseCase = mockk()
    private val getUserStatusUseCase: GetUserStatusUseCase = mockk()
    private val getUserUIdUseCase: GetUserUIdUseCase = mockk()
    private val serviceTableUseCase: ServiceTableUseCase = mockk()
    private val startOrderStatusServiceUseCase: StartOrderStatusServiceUseCase = mockk()
    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    private val userUId = "testUserId"

    @Before
    fun setup() {
        Dispatchers.setMain(testCoroutineDispatcher)

        // Мокирование getUserUIdUseCase для возврата testUserId
        coEvery { getUserUIdUseCase() } returns userUId

        // Мокирование getTablesUseCase для возврата списка столов
        val mockTables = listOf(
            TableModel("table1", "1", false, emptyList(), ""),
            TableModel("table2", "2", true, listOf("customer1"), userUId),
            TableModel("table3", "3", false, emptyList(), "otherUserId"),
            TableModel("table2", "4", true, listOf("customer1"), ""),

        )
        coEvery { getTablesUseCase() } returns flowOf(Response.Success(mockTables))
        coEvery { getUserStatusUseCase() } returns flowOf(Response.Success(true))

        viewModel = WaiterViewModel(
            getTablesUseCase,
            getUserStatusUseCase,
            getUserUIdUseCase,
            serviceTableUseCase,
            startOrderStatusServiceUseCase
        )
    }

    @Test
    fun `loadUnServedTables should update state with unserved tables served by userUId`() = runBlockingTest {
        viewModel.onEvent(WaiterEvent.LoadUnServedTables)
        testCoroutineDispatcher.scheduler.advanceUntilIdle()
        coVerify { getTablesUseCase() }

        val expectedTables = listOf(
            TableModel("table2", "4", true, listOf("customer1"), ""),
        )

        assertEquals(expectedTables, viewModel.container.stateFlow.value.freeTables)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testCoroutineDispatcher.cleanupTestCoroutines()
    }
}
