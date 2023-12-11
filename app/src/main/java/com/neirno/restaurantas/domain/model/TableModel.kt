package com.neirno.restaurantas.domain.model

data class TableModel(
    val tableId: String = "",
    val number: String = "",
    val reserved: Boolean = false,
    val reservedBy: List<String> = listOf(""),
    val servedBy: String = "",
)
