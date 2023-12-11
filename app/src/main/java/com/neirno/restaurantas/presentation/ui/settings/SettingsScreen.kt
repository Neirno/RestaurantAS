package com.neirno.restaurantas.presentation.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.neirno.restaurantas.R
import com.neirno.restaurantas.core.constans.UserType
import com.neirno.restaurantas.presentation.theme.RestaurantASTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewState: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    sideEffect: Flow<SettingsSideEffect>
) {
    LaunchedEffect(sideEffect) {
        sideEffect.collect { sideEffect ->
            handleSideEffect(
                sideEffect = sideEffect,
                navController = navController,
            )
        }
    }

/*
    LaunchedEffect(true) { // Костыль
        onEvent(SettingsEvent.LoadUserInfo)
    }
*/

    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "Настройки",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SettingsEvent.GoBack) }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
            )
        }
    ) { scaffoldPadding ->
        Column (
            modifier = modifier.padding(scaffoldPadding)
        ) {
            ProfileHeader()
            Spacer(modifier = Modifier.height(8.dp))
            ProfileProperty(UserType.fromString(viewState.user.userType).getDisplayName(), "Тип пользователя")
            //ProfileProperty("User", "")
            ProfileProperty(viewState.user.username, "Имя")
            Spacer(modifier = Modifier.height(32.dp))
            //GoogleSignInButton()
            LogoutButton { onEvent(SettingsEvent.SignOut) }
        }
    }
}

@Composable
fun ProfileHeader() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Replace with actual image using painterResource if available
        Icon(
            painter = painterResource(id = R.drawable.ic_human),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
fun GoogleSignInButton() {
    Button(
        onClick = { /* TODO: Handle Google Sign-In */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD4B39))
    ) {
        Image(
            modifier = Modifier.size(32.dp),
            painter = painterResource(id = R.drawable.ic_google),
            contentDescription = "Google Sign-In"
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Connect with Google", color = Color.White)
    }
}

@Composable
fun LogoutButton(
    singOut: () -> Unit
) {
    val color = MaterialTheme.colorScheme.background
    Button(
        onClick = { singOut() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(Icons.Filled.ExitToApp, contentDescription = "Logout", tint = color)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Logout", color = color)
    }
}

@Composable
fun ProfileProperty(value: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f)
        )
        Text(text = label, color = Color.Gray)
    }
}

/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditableProperty(value: String, label: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        shape = MaterialTheme.shapes.extraLarge,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}*/

private fun handleSideEffect(
    sideEffect: SettingsSideEffect,
    navController: NavController,
) {
    when (sideEffect) {
        is SettingsSideEffect.NavigateToBack -> {
            navController.popBackStack()
        }
        is SettingsSideEffect.NavigateToLoginScreen -> {
            navController.navigate("auth_screen") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                // Удаляем возможность возвращения на предыдущий экран
                launchSingleTop = true
            }
        }
    }
}

@Preview(name = "SettingsScreen Dark Theme", showBackground = true)
@Composable
fun SettingsScreenPreviewDark() {
    val navController = rememberNavController()
    val viewState = SettingsState()
    val onEvent: (SettingsEvent) -> Unit = {}
    val sideEffect = emptyFlow<SettingsSideEffect>()

    RestaurantASTheme(darkTheme = true) {
        SettingsScreen(
            navController = navController,
            viewState = viewState,
            onEvent = onEvent,
            sideEffect = sideEffect
        )
    }
}

@Preview(name = "SettingsScreen Light Theme", showBackground = true)
@Composable
fun SettingsScreenPreviewLight() {
    val navController = rememberNavController()
    val viewState = SettingsState()
    val onEvent: (SettingsEvent) -> Unit = {}
    val sideEffect = emptyFlow<SettingsSideEffect>()

    RestaurantASTheme(darkTheme = false) {
        SettingsScreen(
            navController = navController,
            viewState = viewState,
            onEvent = onEvent,
            sideEffect = sideEffect
        )
    }
}
