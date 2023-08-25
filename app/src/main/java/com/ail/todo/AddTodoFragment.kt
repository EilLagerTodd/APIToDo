package com.ail.todo

import AddTodoResponse
import TodoRequest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.ail.todo.databinding.FragmentAddTodoBinding
import com.ail.todo.retrofit.RetrofitManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddTodoFragment : DialogFragment() {

    private var _binding: FragmentAddTodoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle "Done" button click
        binding.btnDone.setOnClickListener {
            val todoTitle = binding.etTodoInput.text.toString()
            val todoStatus = binding.switchCompleted.isChecked

            if (todoTitle.length < 6) {
                Toast.makeText(requireContext(), "할일은 최소 6자 이상으로 적어주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                // Making the API call to add the new ToDo
                val todoRequest = TodoRequest(title = todoTitle, is_done = todoStatus)
                RetrofitManager.instance.retrofitService?.addTodo(todoRequest)
                    ?.enqueue(object : Callback<AddTodoResponse> { // <-- Change here
                        override fun onResponse(
                            call: Call<AddTodoResponse>,  // <-- Change here
                            response: Response<AddTodoResponse>  // <-- Change here
                        ) {
                            if (response.isSuccessful) {
                                // Call fetchData() from MainActivity to refresh data
                                (activity as? MainActivity)?.fetchData()
                                dismiss()
                            } else {
                                // Handle error response
                                Toast.makeText(
                                    requireContext(),
                                    "Error adding todo: ${response.message()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<AddTodoResponse>, t: Throwable) { // <-- Change here
                            Toast.makeText(requireContext(), "API call failed: ${t.message}", Toast.LENGTH_SHORT)
                                .show()

                            Log.d("실패", t.message.toString())
                        }
                    })
            }
        }

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 85% of screen width
        val height = (resources.displayMetrics.heightPixels * 0.3).toInt() // 75% of screen height
        dialog?.window?.setLayout(width, height)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
