package com.neirno.restaurantas.presentation.ui.hostess.pages

import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.animation.Crossfade
import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neirno.restaurantas.core.ui.BlinkingText
import com.neirno.restaurantas.core.ui.FullNameTextField
import com.neirno.restaurantas.core.ui.TablesList
import com.neirno.restaurantas.domain.model.TableModel
import kotlinx.coroutines.delay


@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun HostessPage(
    modifier: Modifier = Modifier,
    tables: List<TableModel>,
    persons: List<String>,
    reservedStatus: Boolean,
    setPersons: (List<String>) -> Unit,
    setTableById: (String?) -> Unit
) {
    var fullNames by remember { mutableStateOf(persons) }


    LaunchedEffect(persons) {
        if (persons == listOf(""))
            fullNames = persons
    } // костыль, но пока ничего в голову не лезет, как пофиксить

    Column (modifier = modifier) {

        BlinkingText(text = "СТОЛЫ:", isBlinking = reservedStatus)
        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
        )

        if (tables.isEmpty()) {
            Text(text = "Столов в заведении нет.")
        } else {
            TablesList(tables = tables, reservedStatus = reservedStatus, chooseTable = {tableId -> setTableById(tableId)})
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "ФИО:", style = MaterialTheme.typography.bodySmall)
        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
        )

        LazyColumn (
            modifier = Modifier.padding(top = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            itemsIndexed(fullNames) {  index, name ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp).animateItemPlacement(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = LinearOutSlowInEasing,
                        )
                    )
                ) {
                    FullNameTextField(
                        modifier = Modifier.weight(1f),
                        fullName = fullNames[index],
                        onValueChange = { newValue ->
                            // Обновляем значение для текущего индекса
                            fullNames = fullNames.toMutableList().apply { set(index, newValue) }
                            setPersons(fullNames)
                        },
                        readOnly = reservedStatus
                    )
                    IconButton(
                        modifier = Modifier.padding(start = 16.dp),
                        enabled = !reservedStatus,
                        onClick = {
                        // Удаляем элемент по индексу
                        if (fullNames.size > 1) { // Предотвращаем удаление последнего поля
                            fullNames = fullNames.toMutableList().apply { removeAt(index) }
                        }
                    }) {
                        Icon(Icons.Filled.Remove, contentDescription = "Удалить")
                    }
                }
            }

            item {
                val canAddMore = fullNames.none { it.isBlank() } && !reservedStatus// true, если нет пустых строк + res.status
                IconButton(
                    onClick = {
                        if (canAddMore) {
                            fullNames = fullNames.toMutableList().also { it.add("") }
                        }
                    },
                    modifier = Modifier.size(48.dp), // Размер кнопки
                    enabled = canAddMore // Кнопка активна, если нет пустых полей
                ) {
                    Icon(modifier = Modifier.size(48.dp), imageVector = Icons.Filled.Add, contentDescription = "Добавить")
                }
            }
        }
    }
}