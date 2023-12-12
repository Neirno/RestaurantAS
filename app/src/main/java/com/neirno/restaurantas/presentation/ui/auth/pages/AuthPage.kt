package com.neirno.restaurantas.presentation.ui.auth.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.neirno.restaurantas.R
import com.neirno.restaurantas.core.ui.EmailTextField
import com.neirno.restaurantas.core.ui.LogoIcon
import com.neirno.restaurantas.core.ui.PasswordTextField

@Composable
fun AuthPage(
    modifier: Modifier = Modifier,
    singIn: (String, String) -> Unit,
    //googleSignIn
) {
    val scrollState = rememberScrollState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    Column (
        modifier = modifier
            .padding(48.dp)
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LogoIcon(Modifier.padding(16.dp))

        EmailTextField(email = email) { newEmail ->
            email = newEmail
        }

        PasswordTextField(
            password = password,
            onDone =  { singIn(email, password) },
            onValueChange = { newPassword ->
                password = newPassword
            }
        )
        Button(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(bottom = 16.dp) ,
            onClick = { singIn(email, password) }
        ) {
            Text("Вход")
        }
    }
}
