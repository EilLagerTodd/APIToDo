package com.ail.todo.retrofit

import android.util.Log
import com.ail.todo.utils.Constants.TAG
import com.ail.todo.utils.isJsonArray
import com.ail.todo.utils.isJsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

object RetrofitClient {

    // 레트로핏 클라이언트 선언

    private var retrofitClient: Retrofit? = null

    //레트로핏 클라이언트 가져오기
    fun getClient(baseUrl: String): Retrofit? {
        Log.d(TAG, "RetrofitCLient - getClient() called")

        //okhttp instance 생성
        val client = OkHttpClient.Builder()

        // 로그를 찍기위해 로깅인터셉터 추가
        val loggingInterceptor = HttpLoggingInterceptor(object: HttpLoggingInterceptor.Logger{
            override fun log(message: String) {
                Log.d(TAG, "로그 생성됨 message: ${message}")
                when{
                    message.isJsonObject() ->
                        Log.d(TAG, JSONObject(message).toString(4))
                    message.isJsonArray() ->
                        Log.d(TAG, JSONObject(message).toString(4))
                    else->
                        try{
                            Log.d(TAG, JSONObject(message).toString(4))
                        } catch (e: Exception) {
                            Log.d(TAG, message)
                        }
                }
            }
        })

        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        // 위에서 설정한 로깅 인터셉터를 okhttp 클라이언트에 추가한다.
        client.addInterceptor(loggingInterceptor)

        if (retrofitClient == null) {
            //레트로핏 빌더를 통해 인스턴스 생성
            retrofitClient = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client.build())
                .build()
        }
        return retrofitClient
    }

}