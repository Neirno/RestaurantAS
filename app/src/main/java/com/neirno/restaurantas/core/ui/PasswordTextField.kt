package com.neirno.restaurantas.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(
    password: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = MaterialTheme.shapes.extraLarge,
        value = password,
        singleLine = true,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() })
    )
}