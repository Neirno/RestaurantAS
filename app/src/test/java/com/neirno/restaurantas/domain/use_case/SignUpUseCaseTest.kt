package com.neirno.restaurantas.domain.use_case

import com.neirno.restaurantas.data.repository.AuthRepositoryImpl
import com.neirno.restaurantas.domain.model.UserModel
import com.neirno.restaurantas.domain.use_case.auth.SignUpUseCase
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals


class SignUpUseCaseTest {

    // Мокируемые зависимости
    private lateinit var authRepository: AuthRepository
    private lateinit var signUpUseCase: SignUpUseCase

    // Данные для тестирования
    private val email = "test@example.com"
    private val password = "password123"
    private val userModel = UserModel()

    @Before
    fun setUp() {
        // Инициализация моков
        authRepository = mockk<AuthRepositoryImpl>()
        signUpUseCase = SignUpUseCase(authRepository)
    }

    @Test
    fun `sign up success returns success response`() = runBlocking {
        // Подготовка моков
        val mockAuthResult = mockk<AuthResult>(relaxed = true)
        val task = Tasks.forResult(mockAuthResult)
        coEvery { authRepository.signUp(email, password, userModel) } returns task

        // Выполнение use case
        val results = signUpUseCase(email, password, userModel).toList()

        // Проверка результатов
        assert(results[0] is Response.Loading)
        assert(results[1] is Response.Success)
    }

    @Test
    fun `sign up failure returns error response`() = runBlocking {
        // Подготовка моков
        val exception = Exception("Auth failed")
        val task = Tasks.forException<AuthResult>(exception)
        coEvery { authRepository.signUp(email, password, userModel) } returns task

        // Выполнение use case
        val results = signUpUseCase(email, password, userModel).toList()

        // Проверка результатов
        assert(results[0] is Response.Loading)
        assert(results[1] is Response.Error)
        assertEquals("Auth failed", (results[1] as Response.Error).msg)
    }
}
