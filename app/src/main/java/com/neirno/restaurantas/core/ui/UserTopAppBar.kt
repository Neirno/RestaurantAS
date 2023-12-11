package com.neirno.restaurantas.core.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserTopAppBar(
    modifier: Modifier = Modifier,
    userType: String,
    workStatus: Boolean,
    navigate: () -> Unit
) {
    CenterAlignedTopAppBar(
        modifier = modifier.padding(bottom = 32.dp),
        title = {
            LogoIcon(Modifier.size(60.dp))
        },
        navigationIcon = {
            Text(
                modifier = Modifier.padding(16.dp),
                text = userType,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            IconButton(
                modifier = Modifier.padding(16.dp),
                onClick = { navigate() }
            ) {
                Icon(
                    /*modifier = Modifier
                        .padding(16.dp)
                        .size(25.dp),*/
                    imageVector = Icons.Default.Circle,
                    tint = if (workStatus) Color.Green else Color.Red,
                    contentDescription = null
                )
            }
        }
    )
}