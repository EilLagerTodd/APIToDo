package com.ail.todo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ail.todo.data.TodoDataClass
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

        RetrofitManager.instance.getTodo { responseState, responseBody ->
            when (responseState) {
                RESPONSE_STATE.OKAY -> {
                    Log.d(TAG, "api 호출 성공 : $responseBody")
                    if(responseBody is TodoDataClass) {
                        val todoData = responseBody
                        val todoAdapter = binding.todoRecyclerView.adapter as TodoAdapter
                        todoAdapter.setTodoList(todoData.dataList)
                    }
                }
                RESPONSE_STATE.FAIL -> {
                    Log.d(TAG, "api 호출 실패 : $responseBody")
                }
            }
        }


        // Initialize RecyclerView and Adapter
        val todoAdapter = TodoAdapter(listOf())
        binding.todoRecyclerView.adapter = todoAdapter
        binding.todoRecyclerView.layoutManager = LinearLayoutManager(this)

        // Track the current page for pagination
        var currentPage = 1

// Implementing endless scrolling
        binding.todoRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    currentPage += 1
                    RetrofitManager.instance.getTodo(page = currentPage) { responseState, responseBody ->
                        when (responseState) {
                            RESPONSE_STATE.OKAY -> {
                                if(responseBody is TodoDataClass) {
                                    val todoData = responseBody
                                    val todoAdapter = binding.todoRecyclerView.adapter as TodoAdapter
                                    todoAdapter.appendTodoList(todoData.dataList)
                                }
                            }
                            RESPONSE_STATE.FAIL -> {
                                Log.d(TAG, "Failed to load more data.")
                            }
                        }
                    }
                }
            }
        })

    }


    override fun onResume() {
        super.onResume()

    }

}