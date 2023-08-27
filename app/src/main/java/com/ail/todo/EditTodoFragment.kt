package com.ail.todo

import TodoRequest
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.ail.todo.data.Data
import com.ail.todo.data.TodoResponse
import com.ail.todo.databinding.DialogEditTodoBinding
import com.ail.todo.retrofit.RetrofitManager
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditTodoFragment : DialogFragment() {

    private lateinit var currentTodo: Data

    private var _binding: DialogEditTodoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditTodoBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        currentTodo = arguments?.getSerializable("TODO_DATA") as Data

        binding.etTodoInput.setText(currentTodo.title)
        binding.switchCompleted.isChecked = currentTodo.is_done

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnEdit.setOnClickListener {
            val updatedTitle = binding.etTodoInput.text.toString()
            val updatedIsDone = binding.switchCompleted.isChecked
            if (updatedTitle.length < 6) {
                Toast.makeText(requireContext(), "할일은 최소 6자 이상으로 적어주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val request = TodoRequest(title = updatedTitle, is_done = updatedIsDone)
            RetrofitManager.instance.updateTodo(todoId = currentTodo.id, todoRequest = request).enqueue(object: Callback<TodoResponse> {
                override fun onResponse(call: Call<TodoResponse>, response: Response<TodoResponse>) {
                    if (response.isSuccessful) {
                        (activity as? MainActivity)?.fetchData()
                        dismiss()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error adding todo: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                override fun onFailure(call: Call<TodoResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "API call failed: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                    Log.d("실패", t.message.toString())
                }
            })
        }


//        binding.btnDelete.setOnClickListener {
//            RetrofitManager.instance.deleteTodo(todoId = currentTodo.id).enqueue(object: Callback<ResponseBody> {
//                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                    if (response.isSuccessful) {
//                        (activity as? MainActivity)?.fetchData()
//                        dismiss()
//                    } else {
//                        Toast.makeText(
//                            requireContext(),
//                            "Error deleting todo: ${response.errorBody()?.string()}",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                    Toast.makeText(requireContext(), "API call failed: ${t.message}", Toast.LENGTH_SHORT)
//                        .show()
//                    Log.d("실패", t.message.toString())
//                }
//            })
//        }


        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onStart() {
        super.onStart()

        // Adjusting dialog size
        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()  // 95% of screen width
        val height = (resources.displayMetrics.heightPixels * 0.95).toInt()  // 95% of screen height
        dialog?.window?.setLayout(width, height)
        // Setting the animations
        dialog?.window?.setWindowAnimations(R.style.DialogAnimation)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(todo: Data): EditTodoFragment {
            val fragment = EditTodoFragment()
            val args = Bundle()
            args.putSerializable("TODO_DATA", todo)
            fragment.arguments = args
            return fragment
        }
    }
}
