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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import okhttp3.ResponseBody


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var todoAdapter: ToDoAdapter

    private val searchHandler = Handler(Looper.getMainLooper())
    private val SEARCH_DELAY = 500L  // delay in milliseconds
    private var searchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        todoAdapter = ToDoAdapter(listOf())
        binding.recyclerView.adapter = todoAdapter

//        val todoAdapter = ToDoAdapter(listOf())
//        binding.recyclerView.adapter = todoAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                // Check if the swiped item is a header. If it is, do not proceed with the deletion
                if (todoAdapter.getItemViewType(position) == todoAdapter.HEADER_VIEW_TYPE) {
                    // Reset the swiped position to prevent deletion
                    todoAdapter.notifyItemChanged(position)
                    return
                }
                if (direction == ItemTouchHelper.LEFT) {
                    Log.d("OnSwiped", "Swiped position: $position")
                    val itemToDelete = todoAdapter.getTodoItem(position)
                    Log.d("OnSwiped", "Todo ID to delete: ${itemToDelete?.id}")
                    deleteTodoItem(itemToDelete!!.id)
                    // This function will call the API to delete the item
                }
            }



            // This method is used to draw the background and icon
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                val itemView = viewHolder.itemView
                val trashIcon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_trash) // Reference to the trash icon resource
                val iconMargin = (itemView.height - trashIcon!!.intrinsicHeight) / 2

                if (dX < 0) { // Left swipe
                    val iconLeft = itemView.right - trashIcon.intrinsicWidth - iconMargin
                    val iconRight = itemView.right - iconMargin
                    val iconTop = itemView.top + iconMargin
                    val iconBottom = itemView.bottom - iconMargin

                    trashIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    trashIcon.draw(c)

                    val backgroundPaint = Paint()
                    backgroundPaint.color = Color.RED
                    c.drawRect(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat(), backgroundPaint)
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)


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

    //부분업데이트
    fun updateLocalTodo(updatedTodo: Data) {
        // Access the list from the adapter
        val todoList = todoAdapter.getTodoList()
        val index = todoList.indexOfFirst { it.id == updatedTodo.id }
        if (index != -1) {
            todoAdapter.updateItemAt(index, updatedTodo)
            // Notify the adapter about the change
            todoAdapter.notifyItemChanged(index)
        }
    }


    override fun onResume() {
        super.onResume()

    }

    private fun showAddTodoPopup() {
        val fragment = AddTodoFragment()
        fragment.show(supportFragmentManager, "ADD_TODO_TAG")
    }



    private fun deleteTodoItem(todoId: Int) {
        RetrofitManager.instance.deleteTodo(todoId).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    fetchData()
                    Toast.makeText(this@MainActivity, "할 일이 삭제되었습니다!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Error deleting todo: ${response.errorBody()?.string()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@MainActivity, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



}