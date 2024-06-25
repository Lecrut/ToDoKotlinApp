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
                val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm")

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

    private fun formatDate() {
        var date = ""
        if (mDay < 10)
            date += "0"+mDay.toString()
        else
            date += mDay.toString()
        date += "."
        if (mMonth < 10)
            date += "0"+mMonth.toString()
        else
            date += mMonth.toString()
        date += "." + mYear.toString()
        date_time = date
    }

    private fun datePicker() {
        val c = Calendar.getInstance()
        mYear = c[Calendar.YEAR]
        mMonth = c[Calendar.MONTH]
        mDay = c[Calendar.DAY_OF_MONTH]
        val datePickerDialog = DatePickerDialog(this,
            { view, year, monthOfYear, dayOfMonth ->
                mYear = year
                mMonth = monthOfYear
                mDay = dayOfMonth
                formatDate()
                timePicker()
            }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
    }


    private fun formatTime() {
        var time = ""
        time += date_time + " "
        if (mHour < 10)
            time += "0"+mHour.toString()
        else
            time += mHour.toString()
        time += ":"
        if (mMinute < 10)
            time += "0"+mMinute.toString()
        else
            time += mMinute.toString()
        binding.etSetEndTime.setText(time)
    }

    private fun timePicker() {
        val c = Calendar.getInstance()
        mHour = c[Calendar.HOUR_OF_DAY]
        mMinute = c[Calendar.MINUTE]

        val timePickerDialog = TimePickerDialog(this,
            { view, hourOfDay, minute ->
                mHour = hourOfDay
                mMinute = minute
                formatTime()
            }, mHour, mMinute, true
        )
        timePickerDialog.show()
    }

}