package com.ail.todo.retrofit

import android.util.Log
import com.ail.todo.utils.API
import com.ail.todo.utils.Constants.TAG
import com.ail.todo.utils.RESPONSE_STATE
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Response

class RetrofitManager {

    companion object {
        val instance = RetrofitManager()
    }

    //레트로핏 인터페이스 가져오기
    private val iRetrofit : IRetrofit? = RetrofitClient.getClient(API.BASE_URL)?.create(IRetrofit::class.java)

    fun getTodo(completion: (RESPONSE_STATE, String) -> Unit) {
        val call = iRetrofit?.getSampleData().let {
            it
        }?:return

        call.enqueue(object : retrofit2.Callback<JsonElement>{
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                Log.d(TAG, "onRes = ${response.raw()}")

                completion(RESPONSE_STATE.OKAY ,response.body().toString())
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Log.d(TAG, "onFail")
            }

        })
    }
}