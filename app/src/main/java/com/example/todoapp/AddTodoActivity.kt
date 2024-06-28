package com.example.todoapp

import android.R
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.example.todoapp.database.Todo
import com.example.todoapp.databinding.ActivityAddTodoBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException
import java.nio.file.Files
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

    private var notificationTime : Int = 0

    private var fileList: MutableList<String> = mutableListOf()
    private lateinit var listView: ListView

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            handleFileSelection(uri)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listView = binding.fileListView
        listView.setOnItemClickListener { _, _, position, _ ->
           openFile(position)
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            handleDelete(fileList[position])
            deleteFileList(position)
            true
        }

        try {
            notificationTime = intent.getSerializableExtra("notification_time") as Int

            oldTodo = intent.getSerializableExtra("current_todo") as Todo
            binding.etTitle.setText(oldTodo.title)
            binding.etNote.setText(oldTodo.note)
            binding.spinnerCategory.setSelection(oldTodo.category)
            binding.etIsDoneSwitch.isChecked = oldTodo.status == true
            binding.etNotificationSwitch.isChecked = oldTodo.notification == true
            oldTodo.attachments?.let {
                toAttachmentsList(it).forEach {
                    addToFileList(it)
                }
            }

            if (oldTodo.execution == "")
                binding.etSetEndTime.setText("Ustaw datę zakończenia")
            else
                binding.etSetEndTime.setText(oldTodo.execution)

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
            var execution = binding.etSetEndTime.text.toString()
            if (execution === "Ustaw datę zakończenia")
                execution = ""

            if(title.isNotEmpty() && todoDescription.isNotEmpty()){
                val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm")

                if(isUpdate){
                    todo = Todo(oldTodo.id, title, todoDescription, formatter.format(Date()), category, notification, status, execution, fromAttachmentsList(fileList))
                }else{
                    todo = Todo(null, title, todoDescription, formatter.format(Date()), category, notification, status, execution, fromAttachmentsList(fileList))
                }
                if (execution !== "" && notification && execution.length == 16)
                    if (checkNotificationPermissions(this)) {
                        scheduleNotification()
                    }

                var intent = Intent()
                intent.putExtra("todo", todo)
                setResult(RESULT_OK, intent)
                finish()
            }else{
                Toast.makeText(this@AddTodoActivity, "Wypełnij tytuł i opis", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
        }

        binding.etSetEndTime.setOnClickListener {
            datePicker();
        }

        binding.imgDelete.setOnClickListener {
            fileList.forEach {
                deleteFile(it)
            }


            var intent = Intent()
            intent.putExtra("todo", oldTodo)
            intent.putExtra("delete_todo", true)
            setResult(RESULT_OK, intent)
            finish()
        }

        binding.imgBackArrow.setOnClickListener {
            onBackPressed()
        }

        binding.etBtnAddAttachment.setOnClickListener {
            getContent.launch("*/*")
        }
    }

    private fun openFile(position: Int) {
        val openIntent = Intent(Intent.ACTION_VIEW)
        openIntent.data = Uri.parse(fileList[position])
        startActivity(openIntent)
    }

    private fun updateFileList(fileName: String) {
        fileList.add(fileName)
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, fileList)
        listView.adapter = adapter
    }

    private fun addToFileList(path: String) {
        fileList.add(path)
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, fileList)
        listView.adapter = adapter
    }

    private fun deleteFileList(position: Int) {
        fileList.removeAt(position)
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, fileList)
        listView.adapter = adapter
    }

    private fun handleFileSelection(uri: Uri?) {
        if (uri != null) {
            try {
                val fileName: String = getFileName(uri)
                val destFile = File(filesDir, fileName)
                Log.v("destFile", filesDir.toString())
                copyFileToInternalStorage(uri, destFile)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun handleDelete(fileName: String) {
        try {
            val destFile = File(filesDir, fileName)
            Log.v("destFileDelete", filesDir.toString())
            if (destFile.exists()) {
                destFile.delete()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun copyFileToInternalStorage(uri: Uri, destFile: File) {
        contentResolver.openInputStream(uri).use { inputStream ->
            Files.newOutputStream(destFile.toPath()).use { outputStream ->
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream!!.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
            }
        }

        val fileName = destFile.nameWithoutExtension
        val dataWMillis = System.currentTimeMillis()
        val newFileName = "$fileName-$dataWMillis.${destFile.extension}"
        destFile.renameTo(File(destFile.parent, newFileName))
        updateFileList(newFileName)
    }

    private fun getFileName(uri: Uri): String {
        var fileName: String? = ""
        val cursor = contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                fileName = cursor.getString(displayNameIndex)
            }
        } finally {
            cursor?.close()
        }
        return fileName ?: ""
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

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification() {
        val time = getTime()
        if (time > Calendar.getInstance().timeInMillis) {
            val intent = Intent(applicationContext, Notification::class.java)
            intent.putExtra("current_todo", todo)
            intent.putExtra("notification_time", notificationTime)

            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                notificationID,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
        }
    }

    fun checkNotificationPermissions(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val isEnabled = notificationManager.areNotificationsEnabled()

            if (!isEnabled) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                context.startActivity(intent)

                return false
            }
        } else {
            val areEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()

            if (!areEnabled) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                context.startActivity(intent)
                return false
            }
        }
        return true
    }

    private fun getTime(): Long {
        val newCalendar = Calendar.getInstance()
        newCalendar.set(mYear, mMonth, mDay, mHour, mMinute)

        return newCalendar.timeInMillis - notificationTime * 60 * 1000
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

    private fun fromAttachmentsList(attachments: MutableList<String>): String {
        return Gson().toJson(attachments)
    }

    private fun toAttachmentsList(attachmentsJson: String): MutableList<String> {
        val type = object : TypeToken<MutableList<String>>() {}.type
        return Gson().fromJson(attachmentsJson, type)
    }

}