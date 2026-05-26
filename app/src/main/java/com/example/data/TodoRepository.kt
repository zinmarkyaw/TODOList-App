package com.example.data

import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
    val allTodos: Flow<List<TodoItem>> = todoDao.getAllTodos()

    suspend fun insertTodo(todoItem: TodoItem): Long {
        return todoDao.insertTodo(todoItem)
    }

    suspend fun updateTodo(todoItem: TodoItem) {
        todoDao.updateTodo(todoItem)
    }

    suspend fun deleteTodo(todoItem: TodoItem) {
        todoDao.deleteTodo(todoItem)
    }

    suspend fun clearCompletedTodos() {
        todoDao.clearCompletedTodos()
    }
}
