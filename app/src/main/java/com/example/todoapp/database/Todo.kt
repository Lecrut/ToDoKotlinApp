package com.example.todoapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_table")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "date") val date: String?,
    @ColumnInfo(name = "category") val category: Int
//    @ColumnInfo(name = "notification") val notification: Boolean?,
//    @ColumnInfo(name = "status") val status: Boolean?,
//    @ColumnInfo(name = "execution") val execution: String?
): java.io.Serializable
