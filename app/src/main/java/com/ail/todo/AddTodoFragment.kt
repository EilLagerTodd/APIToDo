package com.ail.todo

import AddTodoResponse
import TodoRequest
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
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
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backLLY.setOnClickListener {
            dismiss()
        }

        binding.btnDone.setOnClickListener {
            val todoTitle = binding.etTodoInput.text.toString()
            val todoStatus = binding.switchCompleted.isChecked

            if (todoTitle.length < 6) {
                Toast.makeText(requireContext(), "할일은 최소 6자 이상으로 적어주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                val todoRequest = TodoRequest(title = todoTitle, is_done = todoStatus)
                RetrofitManager.instance.retrofitService?.addTodo(todoRequest)
                    ?.enqueue(object : Callback<AddTodoResponse> {
                        override fun onResponse(
                            call: Call<AddTodoResponse>,
                            response: Response<AddTodoResponse>
                        ) {
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

                        override fun onFailure(call: Call<AddTodoResponse>, t: Throwable) { // <-- Change here
                            Toast.makeText(requireContext(), "API call failed: ${t.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    })
            }
        }

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle)
//        enterTransition = AnimationUtils.loadAnimation(context, R.anim.slide_up)
//        exitTransition = AnimationUtils.loadAnimation(context, R.anim.slide_down)
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
}
