package com.neirno.restaurantas.presentation.ui.admin.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.neirno.restaurantas.R
import com.neirno.restaurantas.core.constans.UserType
import com.neirno.restaurantas.core.ui.ConfirmDialog
import com.neirno.restaurantas.domain.model.UserModel

object Tabs {
    const val Free = 0
    const val Occupied = 1
    const val Size = 2
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdminPage(
    modifier: Modifier = Modifier,
    atWorkUsers: List<UserModel>,
    freeUsers: List<UserModel>,
    setStatus: (UserModel) -> Unit,
) {
    val pagerState = rememberPagerState { Tabs.Size } // Начальная страница
    var currentTab by remember { mutableIntStateOf(Tabs.Occupied) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<UserModel?>(null) }

    LaunchedEffect(pagerState.currentPage) {
        currentTab = pagerState.currentPage
    }

    LaunchedEffect(currentTab) {
        pagerState.animateScrollToPage(currentTab)
    }

    Column(modifier) {
        TabRow(selectedTabIndex = currentTab) {
            Tab(
                text = { Text("Свободные") },
                selected = pagerState.currentPage == Tabs.Free,
                onClick = { currentTab = Tabs.Free},
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface
            )
            Tab(
                text = { Text("На работе") },
                selected = pagerState.currentPage == Tabs.Occupied,
                onClick = { currentTab = Tabs.Occupied },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface
            )

        }

        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState
        ) { page ->
            when (page) {
                Tabs.Free -> {
                    if (freeUsers.isEmpty()) {
                        EmptyUsersView() // Показываем пустой компонент, если нет свободных пользователей
                    } else {
                        UsersList(freeUsers) { user ->
                            selectedUser = user
                            showDialog = true
                        }
                    }
                }
                Tabs.Occupied -> {
                    if (atWorkUsers.isEmpty()) {
                        EmptyUsersView() // Показываем пустой компонент, если нет пользователей на работе
                    } else {
                        UsersList(atWorkUsers) { user ->
                            selectedUser = user
                            showDialog = true
                        }
                    }
                }
            }
        }
    }

    if (showDialog && selectedUser != null) {
        ConfirmDialog(
            onConfirm = {
                selectedUser?.let { setStatus(it) }
                showDialog = false
                selectedUser = null
            },
            onDismiss = {
                showDialog = false
                selectedUser = null
            },
            title = "Изменить статус",
            text = "Вы уверены, что хотите изменить статус этого работника?"
        )
    }
}


@Composable
fun UsersList(
    users: List<UserModel>,
    setStatus: (UserModel) -> Unit
) {
    LazyColumn (Modifier.fillMaxSize()) {
        items(users) { user ->
            UserItem(
                user = user,
                onChangeStatus = { setStatus(user) }
            )
        }
    }
}

@Composable
fun UserItem(
    user: UserModel,
    onChangeStatus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onChangeStatus)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(text = "Имя: ${user.username}", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = UserType.fromString(user.userType).getDisplayName(),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Text(text = if (user.working) "На работе" else "Свободен", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun EmptyUsersView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Здесь можно добавить изображение, символизирующее отсутствие пользователей
        Image(
            modifier = Modifier.padding(16.dp),
            painter = painterResource(id = R.drawable.ic_human), // Замените на ваш ресурс
            contentDescription = null
        )
        Text("Нет доступных пользователей", style = MaterialTheme.typography.bodyLarge)
    }
}
