package com.ail.todo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import android.widget.Toast
import com.ail.todo.databinding.ActivityMainBinding
import com.ail.todo.retrofit.RetrofitManager
import com.ail.todo.utils.Constants.TAG
import com.ail.todo.utils.RESPONSE_STATE

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)



        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }


    override fun onResume() {
        super.onResume()

        binding.btn.setOnClickListener {
            RetrofitManager.instance.getTodo { responseState, responseBody ->
                when (responseState) {
                    RESPONSE_STATE.OKAY -> {
                        Log.d(TAG, "api 호출 성공 : $responseBody")
                    }
                    RESPONSE_STATE.FAIL -> {
                        Log.d(TAG, "api 호출 실패 : $responseBody")
                    }
                }
            }
        }

    }

}