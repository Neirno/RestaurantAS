package com.neirno.restaurantas

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.neirno.restaurantas.domain.model.OrderModel
import com.neirno.restaurantas.presentation.ui.cook.pages.CookPage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CookPageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cookPageShowDialogTest() {
        val freeOrders = listOf(OrderModel(orderId = "1", note = "Test Order"))
        val myOrders = listOf<OrderModel>()

        composeTestRule.setContent {
            CookPage(
                freeOrders = freeOrders,
                myOrders = myOrders,
                openOrder = {/* ... */},
                serviceOrder = {/* ... */}
            )
        }

        // Нажатие на элемент списка
        composeTestRule.onNodeWithText("Заказ: Test Order").performClick()

        // Проверяем, что диалоговое окно отображается
        composeTestRule.onNodeWithText("Принять заказ").assertIsDisplayed()

        // Проверка на содержание текста в диалоговом окне
        composeTestRule.onNodeWithText("Вы уверены, что хотите принять этот заказ?").assertIsDisplayed()
    }

    // Дополнительные тесты...
}
