package com.ail.todo.data

import com.google.gson.annotations.SerializedName

data class todoDataClass(
    @SerializedName("dataList")
    val `data`: List<Data>,
    val message: String,
    val meta: Meta
)