package com.ail.todo.data

data class TodoSearchResponse(
    val data: List<Data>,
    val meta: Meta,
    val message: String
)
