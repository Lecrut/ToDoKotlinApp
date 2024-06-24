package com.example.todoapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.todoapp.database.Todo
import com.example.todoapp.databinding.ActivityAddTodoBinding
import java.text.SimpleDateFormat
import java.util.*


class AddTodoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTodoBinding
    private lateinit var todo: Todo
    private lateinit var oldTodo: Todo
    var isUpdate = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            oldTodo = intent.getSerializableExtra("current_todo") as Todo
            binding.etTitle.setText(oldTodo.title)
            binding.etNote.setText(oldTodo.note)
            binding.spinnerCategory.setSelection(oldTodo.category)
            binding.etIsDoneSwitch.isChecked = oldTodo.status == true
            isUpdate = true
        }catch (e: Exception){
            e.printStackTrace()
        }
        if(isUpdate){
            binding.imgDelete.visibility = View.VISIBLE
        }else{
            binding.imgDelete.visibility = View.INVISIBLE
        }

        binding.imgCheck.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val todoDescription = binding.etNote.text.toString()
            val category = binding.spinnerCategory.selectedItemPosition
            val status = binding.etIsDoneSwitch.isChecked

            if(title.isNotEmpty() && todoDescription.isNotEmpty()){
                val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm a")

                if(isUpdate){
                    todo = Todo(oldTodo.id, title, todoDescription, formatter.format(Date()), category, status)
                }else{
                    todo = Todo(null, title, todoDescription, formatter.format(Date()), category, status)
                }
                var intent = Intent()
                intent.putExtra("todo", todo)
                setResult(RESULT_OK, intent)
                finish()
            }else{
                Toast.makeText(this@AddTodoActivity, "please enter some data", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
        }

        binding.imgDelete.setOnClickListener {
            var intent = Intent()
            intent.putExtra("todo", oldTodo)
            intent.putExtra("delete_todo", true)
            setResult(RESULT_OK, intent)
            finish()
        }

        binding.imgBackArrow.setOnClickListener {
            onBackPressed()
        }
    }
}