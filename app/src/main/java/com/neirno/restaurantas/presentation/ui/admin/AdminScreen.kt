package com.neirno.restaurantas.presentation.ui.admin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.neirno.restaurantas.core.ui.UserTopAppBar
import com.neirno.restaurantas.core.util.UiStatus
import com.neirno.restaurantas.presentation.theme.RestaurantASTheme
import com.neirno.restaurantas.presentation.ui.admin.pages.AdminPage
import com.neirno.restaurantas.presentation.ui.admin.pages.ErrorPage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewState: AdminState,
    onEvent: (AdminEvent) -> Unit,
    sideEffect: Flow<AdminSideEffect>
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(sideEffect) {
        sideEffect.collect { sideEffect ->
            handleSideEffect(
                sideEffect = sideEffect,
                snackbarHostState = snackbarHostState,
                navController = navController
            )
        }
    }

    Scaffold (
        topBar = {
            UserTopAppBar(
                userType = "Админ",
                workStatus = viewState.userStatus,
                navigate = { onEvent(AdminEvent.GoToSettings) }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(MaterialTheme.shapes.large)
                ) {
                    Text(data.visuals.message)
                }
            }
        },
    ) { scaffoldPadding ->
        when (viewState.status) {
            is UiStatus.Success -> {
                AdminPage(
                    modifier = modifier.padding(scaffoldPadding),
                    atWorkUsers = viewState.workingWorkers,
                    freeUsers = viewState.nonWorkingWorkers,
                    setStatus = { user -> onEvent(AdminEvent.ChangeWorkingStatus(user))}
                )
            }
            is UiStatus.Error -> {
                ErrorPage(
                    modifier = Modifier.padding(scaffoldPadding).fillMaxSize(),
                    error = viewState.status.message,
                    retry = { onEvent(AdminEvent.LoadUsers) }
                )
            }
            is UiStatus.Loading -> {}
        }
    }
}

private suspend fun handleSideEffect(
    sideEffect: AdminSideEffect,
    snackbarHostState: SnackbarHostState,
    navController: NavController
) {
    when (sideEffect) {
        is AdminSideEffect.ShowError -> {
            snackbarHostState.showSnackbar(sideEffect.err)
        }
        AdminSideEffect.NavigateToSettings -> {
            navController.navigate("settings_screen")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminScreenPreviewError() {
    val fakeNavController = rememberNavController()
    val fakeViewState = AdminState(
        status = UiStatus.Error("Произошла ошибка"),
        workingWorkers = listOf(),
        nonWorkingWorkers = listOf(),
        userStatus = true
    )

    RestaurantASTheme (
        darkTheme = true
    ) {
        AdminScreen(
            modifier = Modifier.fillMaxSize(),
            navController = fakeNavController,
            viewState = fakeViewState,
            onEvent = { /* Обработка событий */ },
            sideEffect = flowOf() // Пустой flow для sideEffect
        )
    }
}