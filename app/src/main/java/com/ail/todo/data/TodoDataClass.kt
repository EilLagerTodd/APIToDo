package com.ail.todo.data

import com.google.gson.annotations.SerializedName

data class TodoDataClass(
    @SerializedName("data")
    val dataList: List<Data>,
    val message: String,
    val meta: Meta
)
