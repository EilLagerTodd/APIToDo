package com.ail.todo.retrofit

import android.util.Log
import com.ail.todo.data.TodoDataClass
import com.ail.todo.utils.API
import com.ail.todo.utils.Constants.TAG
import com.ail.todo.utils.RESPONSE_STATE
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RetrofitManager {

    companion object {
        val instance = RetrofitManager()
    }

    //레트로핏 인터페이스 가져오기
    private val iRetrofit : IRetrofit? = RetrofitClient.getClient(API.BASE_URL)?.create(IRetrofit::class.java)

    fun getTodo(
        page: Int = 1,
        filter: String = "created_at",
        order_by: String = "desc",
        is_done: Boolean? = null,
        per_page: Int = 10,
        completion: (RESPONSE_STATE, TodoDataClass?) -> Unit
    ) {
        val call = iRetrofit?.getTodoList(page, filter, order_by, is_done, per_page) ?: return

        call.enqueue(object : Callback<TodoDataClass> {
            override fun onResponse(call: Call<TodoDataClass>, response: Response<TodoDataClass>) {
                if (response.isSuccessful) {
                    completion(RESPONSE_STATE.OKAY, response.body())
                } else {
                    completion(RESPONSE_STATE.FAIL, null)
                }
            }

            override fun onFailure(call: Call<TodoDataClass>, t: Throwable) {
                Log.d(TAG, "onFail: ${t.message}")
                completion(RESPONSE_STATE.FAIL, null)
            }
        })
    }


}