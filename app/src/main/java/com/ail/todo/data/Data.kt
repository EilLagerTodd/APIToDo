package com.ail.todo.data

data class Data(
    val created_at: String,
    val id: Int,
    val is_done: Boolean,
    val title: String,
    val updated_at: String
)