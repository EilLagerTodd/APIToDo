package com.ail.todo.data

data class Meta(
    val current_page: Int,
    val from: Int,
    val last_page: Int,
    val per_page: Int,
    val to: Int,
    val total: Int
)