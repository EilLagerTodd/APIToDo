package com.ail.todo.retrofit

import com.ail.todo.data.Meta
import com.ail.todo.data.TodoDataClass
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface IRetrofit {

    //baseUrl = https://phplaravel-574671-2962113.cloudwaysapps.com/api/v2

    @GET("todos")
    fun getTodoList(
        @Query("page") page: Int = 1,
        @Query("filter") filter: String = "created_at",
        @Query("order_by") order_by: String = "desc",
        @Query("is_done") is_done: Boolean? = null,
        @Query("per_page") per_page: Int = 10
    ): Call<TodoDataClass>

}