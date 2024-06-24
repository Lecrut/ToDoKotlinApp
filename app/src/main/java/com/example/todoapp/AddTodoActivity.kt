package com.example.todoapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.todoapp.database.Todo
import com.example.todoapp.databinding.ActivityAddTodoBinding
import java.text.SimpleDateFormat
import java.util.*


class AddTodoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTodoBinding
    private lateinit var todo: Todo
    private lateinit var oldTodo: Todo
    var isUpdate = false

    var date_time = ""
    var mYear = 0
    var mMonth = 0
    var mDay = 0

    var mHour = 0
    var mMinute = 0


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
            binding.etNotificationSwitch.isChecked = oldTodo.notification == true
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
            val notification = binding.etNotificationSwitch.isChecked

            if(title.isNotEmpty() && todoDescription.isNotEmpty()){
                val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm a")

                if(isUpdate){
                    todo = Todo(oldTodo.id, title, todoDescription, formatter.format(Date()), category, notification, status)
                }else{
                    todo = Todo(null, title, todoDescription, formatter.format(Date()), category, notification, status)
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

        binding.etSetEndTime.setOnClickListener {
            datePicker();
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

    private fun datePicker() {

        // Get Current Date
        val c = Calendar.getInstance()
        mYear = c[Calendar.YEAR]
        mMonth = c[Calendar.MONTH]
        mDay = c[Calendar.DAY_OF_MONTH]
        val datePickerDialog = DatePickerDialog(this,
            { view, year, monthOfYear, dayOfMonth ->
                date_time = dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                //*************Call Time Picker Here ********************
                timePicker()
            }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
    }

    private fun timePicker() {
        // Get Current Time
        val c = Calendar.getInstance()
        mHour = c[Calendar.HOUR_OF_DAY]
        mMinute = c[Calendar.MINUTE]

        // Launch Time Picker Dialog
        val timePickerDialog = TimePickerDialog(this,
            { view, hourOfDay, minute ->
                mHour = hourOfDay
                mMinute = minute
                binding.etSetEndTime.setText("$date_time $hourOfDay:$minute")
            }, mHour, mMinute, false
        )
        timePickerDialog.show()
    }

}