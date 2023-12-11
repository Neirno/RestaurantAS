package com.neirno.restaurantas.core.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.neirno.restaurantas.domain.model.TableModel


@Composable
fun TableInfoDialog(
    table: TableModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Table Information") },
        text = {
            Text(
                text = "Number: ${table.number}\n" +
                        "Reserved: ${if (table.reserved) "Yes" else "No"}\n" +
                        "Reserved By: ${if (table.reservedBy == listOf("")) { "N/A" } else table.reservedBy}\n" +
                        "Served By: ${table.servedBy.ifEmpty { "N/A" }}"
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("OK")
            }
        }
    )
}