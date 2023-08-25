package com.ail.todo.retrofit

import AddTodoResponse
import TodoRequest
import com.ail.todo.data.Data
import com.ail.todo.data.TodoDataClass
import com.ail.todo.data.TodoResponse
import com.ail.todo.data.TodoSearchResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
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

    @POST("todos")
    fun addTodo(
        @Body todo: TodoRequest
    ): Call<AddTodoResponse>

    @GET("todos/{id}")
    fun getTodo(@Path("id") id: Int): Call<TodoResponse>

    @PUT("todos/{id}")
    fun updateTodo(
        @Path("id") id: Int,
        @Body todoRequest: TodoRequest
    ): Call<TodoResponse>

    @DELETE("todos/{id}")
    fun deleteTodo(@Path("id") id: Int): Call<ResponseBody>

    @POST("/todos-json/{id}")
    fun updateTodoJson(
        @Path("id") id: Int,
        @Body todoRequest: TodoRequest
    ): Call<TodoResponse>

    @GET("todos/search")
    fun searchTodos(
        @Query("query") query: String,
        @Query("filter") filter: String? = null,
        @Query("created_at") createdAt: String? = null,
        @Query("order_by") orderBy: String? = null,
        @Query("desc") desc: String? = null,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("is_done") isDone: Boolean? = null
    ): Call<TodoSearchResponse>

}