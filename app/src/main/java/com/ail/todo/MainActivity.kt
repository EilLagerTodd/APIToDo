package com.ail.todo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ail.todo.data.Data
import com.ail.todo.data.TodoDataClass
import com.ail.todo.data.TodoResponse
import com.ail.todo.data.TodoSearchResponse
import com.ail.todo.databinding.ActivityMainBinding
import com.ail.todo.retrofit.RetrofitManager
import com.ail.todo.utils.Constants.TAG
import com.ail.todo.utils.RESPONSE_STATE
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding

    private val searchHandler = Handler(Looper.getMainLooper())
    private val SEARCH_DELAY = 500L  // delay in milliseconds
    private var searchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val todoAdapter = ToDoAdapter(listOf())
        binding.recyclerView.adapter = todoAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        fetchData()

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchData()
        }

        binding.fabAddTodo.setOnClickListener {
            showAddTodoPopup()
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        var currentPage = 1

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

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchHandler.removeCallbacksAndMessages(null)
                searchQuery = query ?: ""
                fetchData()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchHandler.removeCallbacksAndMessages(null)
                searchQuery = newText ?: ""
                searchHandler.postDelayed({
                    fetchData()
                }, SEARCH_DELAY)
                return true
            }
        })
    }

    fun fetchData() {
        if (searchQuery.isEmpty()) {
            RetrofitManager.instance.getTodo { responseState, responseBody ->
                when (responseState) {
                    RESPONSE_STATE.OKAY -> {
                        if (responseBody is TodoDataClass) {
                            val todoData = responseBody.dataList
                            val todoAdapter = binding.recyclerView.adapter as ToDoAdapter
                            todoAdapter.setTodoList(responseBody.dataList)
                        }
                        binding.swipeRefreshLayout.isRefreshing = false
                    }

                    RESPONSE_STATE.FAIL -> {
                        Log.d(TAG, "API call failure: $responseBody")
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                }
            }
        } else {
            Log.d("검색", searchQuery)
            RetrofitManager.instance.searchTodos(query = searchQuery)
                .enqueue(object : Callback<TodoSearchResponse> {
                    override fun onResponse(
                        call: Call<TodoSearchResponse>,
                        response: Response<TodoSearchResponse>
                    ) {
                        if (response.isSuccessful) {
                            val searchResults = response.body()?.data
                            val todoAdapter = binding.recyclerView.adapter as ToDoAdapter
                            if (searchResults != null) {
                                todoAdapter.setTodoList(searchResults)
                            }
                            binding.swipeRefreshLayout.isRefreshing = false
                        } else {
                            Log.d(TAG, "Search API call failure: ${response.errorBody()?.string()}")
                            binding.swipeRefreshLayout.isRefreshing = false
                        }
                    }

                    override fun onFailure(call: Call<TodoSearchResponse>, t: Throwable) {
                        Log.d(TAG, "Search API call failure: ${t.message}")
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                })
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