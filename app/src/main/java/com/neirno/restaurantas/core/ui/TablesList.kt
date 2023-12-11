package com.neirno.restaurantas.core.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.neirno.restaurantas.domain.model.TableModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TablesList(
    modifier: Modifier = Modifier,
    tables: List<TableModel>,
    reservedStatus: Boolean,
    chooseTable: (String?) -> Unit
) {

    var showDialog by remember { mutableStateOf(false) }
    var currentTable by remember { mutableStateOf<TableModel?>(null) }

    LaunchedEffect(reservedStatus) {
        if (!reservedStatus)
            currentTable = null
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        items(tables) { table ->
            TableItem(
                table = table,
                openDialog = {
                    if (!table.reserved && reservedStatus) {
                        currentTable = table
                        chooseTable(table.tableId)
                    } else if (table.reserved && reservedStatus) {
                        chooseTable(null)
                        currentTable = null
                    } else {
                        currentTable = table
                        showDialog = true
                    }
                },
                currentTableId = currentTable?.tableId?: "0",
                reservedStatus = reservedStatus
            )
        }
    }

    if (showDialog && currentTable != null) {
        TableInfoDialog(table = currentTable!!) {
            currentTable = null
            showDialog = false
        }
    }
}

@Composable
fun TableItem(
    table: TableModel,
    openDialog: () -> Unit,
    currentTableId: String,
    reservedStatus: Boolean
) {
    val scope = rememberCoroutineScope()
    var showAnim by remember { mutableStateOf(false) }
    var isExpanding by remember { mutableStateOf(false) }
    val borderColor = if (table.reserved) Color.Red else Color.Green

    var startBorderAnimation by remember { mutableStateOf(false) }
    val animatedVerticalBorder by animateFloatAsState(
        targetValue = if (startBorderAnimation) 1f else 0f,
        animationSpec = if (isExpanding)
            tween(durationMillis = 300) // Задержка при расширении
        else
            tween(durationMillis = 300, delayMillis = 300), label = "" // Без задержки при схлопывании
    )
    val animatedHorizontalBorder by animateFloatAsState(
        targetValue = if (startBorderAnimation) 1f else 0f,
        animationSpec = if (isExpanding)
            tween(durationMillis = 300, delayMillis = 300) // Задержка при расширении
        else
            tween(durationMillis = 300), label = "" // Без задержки при схлопывании
    )

    val shakeDegrees by if (showAnim) {
        rememberInfiniteTransition(label = "").animateFloat(
            initialValue = 0f,
            targetValue = 50f,
            animationSpec = infiniteRepeatable(
                animation = tween(70, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )
    } else {
        animateFloatAsState(0f, label = "")
    }

    LaunchedEffect(currentTableId) {
        if (currentTableId != table.tableId) {
            isExpanding = false
            startBorderAnimation = false
        }
    }


    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(50.dp)
            // drawBehind для рисования линии снизу
            .drawBehind {
                val strokeWidth = 1.dp.toPx() // Ширина линии
                val yOffset = size.height - strokeWidth / 2 // Позиция линии снизу
                drawLine(
                    color = borderColor,
                    start = Offset(0f, yOffset),
                    end = Offset(size.width, yOffset),
                    strokeWidth = strokeWidth
                )
            }
            .drawWithContent {
                drawContent() // Рисует содержимое Box

                val strokeWidth = 1.dp.toPx()
                val verticalBorderHeight = size.height * animatedVerticalBorder
                val horizontalBorderWidth = size.width * animatedHorizontalBorder

                // Рисует вертикальные линии
                drawLine(
                    color = borderColor,
                    start = Offset(x = 0f, y = size.height),
                    end = Offset(x = 0f, y = size.height - verticalBorderHeight),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = borderColor,
                    start = Offset(x = size.width, y = size.height),
                    end = Offset(x = size.width, y = size.height - verticalBorderHeight),
                    strokeWidth = strokeWidth
                )

                // Рисует горизонтальные линии
                drawLine(
                    color = borderColor,
                    start = Offset(x = 0f, y = 0f),
                    end = Offset(x = horizontalBorderWidth, y = 0f),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = borderColor,
                    start = Offset(x = size.width, y = 0f),
                    end = Offset(x = size.width - horizontalBorderWidth, y = 0f),
                    strokeWidth = strokeWidth
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                if (reservedStatus && table.reserved) {
                    showAnim = true
                    scope.launch {
                        delay(350)
                        showAnim = false
                    }
                } else if (reservedStatus) {
                    isExpanding = !isExpanding
                    startBorderAnimation = !startBorderAnimation
                    openDialog()
                } else {
                    openDialog()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.graphicsLayer(
                rotationZ = shakeDegrees,
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            ),
            text = table.number
        )
    }
}
