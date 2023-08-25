package com.ail.todo

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
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

        // Initialize RecyclerView and Adapter
        val todoAdapter = ToDoAdapter(listOf())
        binding.recyclerView.adapter = todoAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch initial data
        fetchData()

        // Implementing SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchData()
        }

        binding.fabAddTodo.setOnClickListener {
            showAddTodoPopup()
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // Track the current page for pagination
        var currentPage = 1

        // Implementing endless scrolling
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                                if (responseBody is TodoDataClass) {
                                    val todoData = responseBody
                                    todoAdapter.appendTodoList(todoData.dataList)  // use todoData.dataList here
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

    fun fetchData() {
        RetrofitManager.instance.getTodo { responseState, responseBody ->
            when (responseState) {
                RESPONSE_STATE.OKAY -> {
                    if (responseBody is TodoDataClass) {
                        val todoData = responseBody
                        val todoAdapter = binding.recyclerView.adapter as ToDoAdapter
                        todoAdapter.setTodoList(todoData.dataList)  // use todoData.dataList here
                    }
                    // Disable the refresh animation after data is fetched
                    binding.swipeRefreshLayout.isRefreshing = false
                }
                RESPONSE_STATE.FAIL -> {
                    Log.d(TAG, "API call failure: $responseBody")
                    // Disable the refresh animation in case of an error
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

    }

    private fun showAddTodoPopup() {
        val fragment = AddTodoFragment()
        fragment.show(supportFragmentManager, "ADD_TODO_TAG")
    }
}