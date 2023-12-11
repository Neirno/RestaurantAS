package com.neirno.restaurantas.presentation.ui.hostess

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.neirno.restaurantas.core.ui.Loading
import com.neirno.restaurantas.core.ui.UserTopAppBar
import com.neirno.restaurantas.core.util.UiStatus
import com.neirno.restaurantas.domain.model.TableModel
import com.neirno.restaurantas.presentation.theme.RestaurantASTheme
import com.neirno.restaurantas.presentation.ui.admin.AdminEvent
import com.neirno.restaurantas.presentation.ui.hostess.pages.ErrorPage
import com.neirno.restaurantas.presentation.ui.hostess.pages.HostessPage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostessScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewState: HostessState,
    onEvent: (HostessEvent) -> Unit,
    sideEffect: Flow<HostessSideEffect>
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler {
        if (viewState.reservedStatus)
            onEvent(HostessEvent.EndReservedMode)
        else
            (context as? ComponentActivity)?.finish()
    }

    LaunchedEffect(sideEffect) {
        sideEffect.collect { sideEffect ->
            handleSideEffect(
                sideEffect = sideEffect,
                snackbarHostState = snackbarHostState,
                navController = navController,
                context = context,
            )
        }
    }

    Scaffold (
        topBar = {
            UserTopAppBar(
                userType = "Хостесс",
                workStatus = viewState.userStatus,
                navigate = { onEvent(HostessEvent.GoToSettings) }
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
        floatingActionButton = {
            val icon = if (!viewState.reservedStatus) Icons.Default.Add else Icons.Default.Check
            val contentDescription = if (!viewState.reservedStatus) "Add" else "Check"
            val onClick = if (!viewState.reservedStatus) {
                { onEvent(HostessEvent.StartReservedMode) }
            } else {
                { onEvent(HostessEvent.ReserveTable(viewState.chosenTableById, viewState.persons)) }
            }
            if (viewState.userStatus) {
                FloatingActionButton(onClick = onClick, Modifier.padding(16.dp)) {
                    Icon(icon, contentDescription = contentDescription)
                }
            }
        }

    ) {
        when (viewState.status) {
            is UiStatus.Success -> {
                HostessPage(
                    modifier = modifier
                        .padding(it)
                        .padding(16.dp)
                        .fillMaxSize(),
                    tables = viewState.tables,
                    persons = viewState.persons,
                    reservedStatus = viewState.reservedStatus,
                    setPersons = { persons -> onEvent(HostessEvent.SetPersons(persons)) },
                    setTableById = { tableId -> onEvent(HostessEvent.SetTableById(tableId)) }
                )
            }
            is UiStatus.Loading -> {
                Loading()
            }
            is UiStatus.Error -> {
                ErrorPage(
                    modifier = modifier
                        .padding(it)
                        .padding(16.dp)
                        .fillMaxSize(),
                    error = viewState.status.message,
                    retry = { onEvent(HostessEvent.GetUserStatus) }
                )
            }
        }
    }
}


private suspend fun handleSideEffect(
    sideEffect: HostessSideEffect,
    snackbarHostState: SnackbarHostState,
    navController: NavController,
    context: Context
) {
    when (sideEffect) {
        is HostessSideEffect.ShowSnackbar -> {
            snackbarHostState.showSnackbar(sideEffect.text)
        }
        HostessSideEffect.NavigateToSettings -> {
            navController.navigate("settings_screen")
        }
    }
}



@Preview
@Composable
fun HostessScreenLightPreview() {
    val fakeNavigationManager = NavController(LocalContext.current)
    val fakeViewState = HostessState(tables = listOf(TableModel()))
    val fakeOnEvent: (HostessEvent) -> Unit = {}
    val fakeSideEffect = flowOf<HostessSideEffect>()

    RestaurantASTheme {
        HostessScreen(
            navController = fakeNavigationManager,
            viewState = fakeViewState,
            onEvent = fakeOnEvent,
            sideEffect = fakeSideEffect
        )
    }
}

@Preview
@Composable
fun HostessScreenDarkPreview() {
    val fakeNavigationManager = NavController(LocalContext.current)
    val fakeViewState = HostessState(tables = listOf(TableModel()))
    val fakeOnEvent: (HostessEvent) -> Unit = {}
    val fakeSideEffect = flowOf<HostessSideEffect>()

    RestaurantASTheme (
        darkTheme = true
    ) {
        HostessScreen(
            navController = fakeNavigationManager,
            viewState = fakeViewState,
            onEvent = fakeOnEvent,
            sideEffect = fakeSideEffect
        )
    }
}

