package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_items ORDER BY isCompleted ASC, priority DESC, createdAt DESC")
    fun getAllTodos(): Flow<List<TodoItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todoItem: TodoItem): Long

    @Update
    suspend fun updateTodo(todoItem: TodoItem)

    @Delete
    suspend fun deleteTodo(todoItem: TodoItem)

    @Query("DELETE FROM todo_items WHERE isCompleted = 1")
    suspend fun clearCompletedTodos()
}
