package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val category: String = "Personal",
    val priority: Int = 1, // 0 = Low, 1 = Medium, 2 = High
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
