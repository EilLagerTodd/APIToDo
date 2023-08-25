package com.ail.todo.retrofit

import TodoRequest
import android.util.Log
import com.ail.todo.data.Data
import com.ail.todo.data.TodoDataClass
import com.ail.todo.data.TodoResponse
import com.ail.todo.utils.API
import com.ail.todo.utils.Constants.TAG
import com.ail.todo.utils.RESPONSE_STATE
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RetrofitManager {

    companion object {
        val instance = RetrofitManager()
    }

    // Getting the Retrofit interface
    val retrofitService : IRetrofit? = RetrofitClient.getClient(API.BASE_URL)?.create(IRetrofit::class.java)

    fun getTodo(
        page: Int = 1,
        filter: String = "created_at",
        order_by: String = "desc",
        is_done: Boolean? = null,
        per_page: Int = 10,
        completion: (RESPONSE_STATE, TodoDataClass?) -> Unit
    ) {
        val call = retrofitService?.getTodoList(page, filter, order_by, is_done, per_page) ?: return

        call.enqueue(object : Callback<TodoDataClass> {
            override fun onResponse(call: Call<TodoDataClass>, response: Response<TodoDataClass>) {
                if (response.isSuccessful) {
                    completion(RESPONSE_STATE.OKAY, response.body())                 // Extracting dataList here
                } else {
                    completion(RESPONSE_STATE.FAIL, null)
                }
            }

            override fun onFailure(call: Call<TodoDataClass>, t: Throwable) {  // Note: Corrected the type to TodoDataClass
                Log.d(TAG, "onFail: ${t.message}")
                completion(RESPONSE_STATE.FAIL, null)
            }
        })
    }


    fun updateTodo(todoId: Int, todoRequest: TodoRequest): Call<TodoResponse> {
        return retrofitService?.updateTodo(todoId, todoRequest)
            ?: throw NullPointerException("Retrofit service is null")
    }


}
