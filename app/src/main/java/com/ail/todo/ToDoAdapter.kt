package com.ail.todo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ail.todo.databinding.ItemTodoBinding
import com.ail.todo.data.Data

class TodoAdapter(private var initialTodos: List<Data>) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    private var todoList: MutableList<Data> = initialTodos.toMutableList()

    inner class TodoViewHolder(private val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(todo: Data) {
            binding.tvTitle.text = todo.title
            binding.tvDate.text = todo.created_at
            binding.cbIsDone.isChecked = todo.is_done
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(binding)
    }

    override fun getItemCount(): Int = todoList.size

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(todoList[position])
    }

    fun setTodoList(newList: List<Data>) {
        todoList = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun appendTodoList(newTodos: List<Data>) {
        todoList.addAll(newTodos)
        notifyDataSetChanged()
    }
}
