package com.ail.todo

import TodoRequest
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ail.todo.databinding.HeaderItemBinding
import com.ail.todo.databinding.ItemTodoBinding
import com.ail.todo.data.Data
import com.ail.todo.data.TodoResponse
import com.ail.todo.retrofit.RetrofitManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ToDoAdapter(private var initialTodos: List<Data>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val HEADER_VIEW_TYPE = 0
    private val ITEM_VIEW_TYPE = 1

    private var todoList: LinkedHashMap<String, List<Data>> = groupByDate(initialTodos)

    private fun groupByDate(todos: List<Data>): LinkedHashMap<String, List<Data>> {
        return todos.groupBy { it.created_at.substring(0, 10) }
            .toSortedMap(reverseOrder())
            .toMutableMap() as LinkedHashMap<String, List<Data>>
    }

    inner class TodoHeaderViewHolder(val binding: HeaderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            binding.tvDateHeader.text = date
        }
    }

    inner class TodoViewHolder(val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var currentTodo: Data? = null

        init {
            binding.root.setOnClickListener {
                currentTodo?.let { todoItem ->
                    val fragment = EditTodoFragment.newInstance(todoItem)
                    fragment.show(
                        (binding.root.context as AppCompatActivity).supportFragmentManager,
                        "editTodoDialog"
                    )
                }
            }
        }

        fun bind(todo: Data) {
            currentTodo = todo
            var time = todo.created_at.substring(11, 13).toInt() + 9
            var minute = todo.created_at.substring(14, 16).toInt()
            binding.tvTitle.text = todo.title
            binding.cbIsDone.isChecked = todo.is_done

            binding.cbIsDone.setOnCheckedChangeListener { _, isChecked ->
                currentTodo?.let { todo ->
                    val updatedTodo = todo.copy(is_done = isChecked)
                    val request =
                        TodoRequest(title = updatedTodo.title, is_done = updatedTodo.is_done)
                    RetrofitManager.instance.updateTodo(
                        todoId = updatedTodo.id,
                        todoRequest = request
                    ).enqueue(object : Callback<TodoResponse> {
                        override fun onResponse(
                            call: Call<TodoResponse>,
                            response: Response<TodoResponse>
                        ) {
                            if (response.isSuccessful) {
                                currentTodo = response.body()?.data
                            } else {
                                Toast.makeText(
                                    binding.root.context,
                                    "Error updating status: ${response.message()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.cbIsDone.isChecked = !isChecked
                            }
                        }

                        override fun onFailure(call: Call<TodoResponse>, t: Throwable) {
                            Toast.makeText(
                                binding.root.context,
                                "API call failed: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.cbIsDone.isChecked = !isChecked
                        }
                    })
                }
            }

            if (time >= 24) {
                time -= 24
                binding.tvDate.text = "AM ${time} : ${minute}"
            } else if (time >= 12) {
                time -= 12
                if (time == 0) {
                    binding.tvDate.text = "PM 12 : ${minute}"
                } else {
                    binding.tvDate.text = "PM ${time} : ${minute}"
                }
            } else {
                if (time == 0) {
                    binding.tvDate.text = "AM 12 : ${minute}"
                } else {
                    binding.tvDate.text = "AM ${time} : ${minute}"
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        var pos = position
        for (entry in todoList.entries) {
            if (pos == 0) return HEADER_VIEW_TYPE
            pos--
            if (pos < entry.value.size) return ITEM_VIEW_TYPE
            pos -= entry.value.size
        }
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == HEADER_VIEW_TYPE) {
            val binding =
                HeaderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            TodoHeaderViewHolder(binding)
        } else {
            val binding =
                ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            TodoViewHolder(binding)
        }
    }

    override fun getItemCount(): Int = todoList.map { it.value.size + 1 }.sum()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var pos = position
        for (entry in todoList.entries) {
            if (pos == 0 && holder is TodoHeaderViewHolder) {
                holder.bind(entry.key)
                return
            }
            pos--
            if (pos < entry.value.size && holder is TodoViewHolder) {
                holder.bind(entry.value[pos])
                return
            }
            pos -= entry.value.size
        }
    }

    fun setTodoList(newList: List<Data>) {
        val newGroupedTodos = groupByDate(newList)
        todoList.clear()
        todoList.putAll(newGroupedTodos)
        notifyDataSetChanged()
    }

    fun appendTodoList(newTodos: List<Data>) {
        val grouped = groupByDate(newTodos)
        for ((key, value) in grouped) {
            if (todoList.containsKey(key)) {
                todoList[key] = todoList[key]!! + value
            } else {
                todoList[key] = value
            }
        }
        notifyDataSetChanged()
    }
}
