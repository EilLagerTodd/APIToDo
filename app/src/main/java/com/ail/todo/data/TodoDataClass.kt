package com.ail.todo.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TodoDataClass(
    @SerializedName("data")
    val dataList: List<Data>,
    val message: String,
    val meta: Meta
): Serializable
