package com.ail.todo.retrofit

import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.GET

interface IRetrofit {

    //baseUrl = https://phplaravel-574671-2962113.cloudwaysapps.com/api/v2

    @GET("todos")
    fun getSampleData() : Call<JsonElement>

}