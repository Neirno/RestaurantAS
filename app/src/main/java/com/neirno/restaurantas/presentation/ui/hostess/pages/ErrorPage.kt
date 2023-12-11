package com.neirno.restaurantas.presentation.ui.hostess.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neirno.restaurantas.R

@Composable
fun ErrorPage(
    modifier: Modifier = Modifier,
    error: String = "",
    retry: () -> Unit
) {
    Column (
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.padding(16.dp),
                painter = painterResource(id = R.drawable.ic_network_error),
                contentDescription = null
            )
            Text(modifier = Modifier.padding(16.dp), text = error)
            FilledTonalButton(
                modifier = Modifier.padding(16.dp),
                onClick = {
                    retry()
                }
            ) {
                Text("Повторить попытку.")
            }
    }
}
