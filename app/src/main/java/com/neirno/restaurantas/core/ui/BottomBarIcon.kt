package com.neirno.restaurantas.core.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomBarIcon(
    icon: ImageVector,
    contentDescription: String?,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val iconColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(75.dp)
            .combinedClickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        IconButton(onClick = { /*TODO*/ }, Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,  modifier = Modifier.fillMaxSize()) {
                    Icon(
                        icon,
                        contentDescription = contentDescription,
                        tint = iconColor,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(MaterialTheme.shapes.extraLarge)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = LocalIndication.current
                            ) {}
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Text(text = "qq", textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                }
            }
    }
}
