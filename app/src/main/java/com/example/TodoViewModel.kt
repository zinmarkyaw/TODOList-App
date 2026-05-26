package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.TodoItem
import com.example.data.TodoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TodoStats(
    val total: Int = 0,
    val completed: Int = 0,
    val active: Int = 0,
    val completionRate: Int = 0
)

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TodoRepository
    private val sharedPrefs = application.getSharedPreferences("todo_settings", android.content.Context.MODE_PRIVATE)

    private val _selectedTheme = MutableStateFlow(getSavedTheme())
    val selectedTheme = _selectedTheme.asStateFlow()

    private fun getSavedTheme(): com.example.ui.theme.AppThemeOption {
        val name = sharedPrefs.getString("app_theme", com.example.ui.theme.AppThemeOption.LAVENDER.name)
        return try {
            com.example.ui.theme.AppThemeOption.valueOf(name ?: com.example.ui.theme.AppThemeOption.LAVENDER.name)
        } catch (e: Exception) {
            com.example.ui.theme.AppThemeOption.LAVENDER
        }
    }

    fun setSelectedTheme(theme: com.example.ui.theme.AppThemeOption) {
        _selectedTheme.value = theme
        sharedPrefs.edit().putString("app_theme", theme.name).apply()
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TodoRepository(database.todoDao())
    }

    // UI state filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _statusFilter = MutableStateFlow("All") // "All", "Active", "Completed"
    val statusFilter = _statusFilter.asStateFlow()

    private val _sortBy = MutableStateFlow("Date") // "Date", "Priority", "Alphabetical"
    val sortBy = _sortBy.asStateFlow()

    // Base todos flow
    val rawTodos: StateFlow<List<TodoItem>> = repository.allTodos.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered and sorted todos flow
    val todos: StateFlow<List<TodoItem>> = combine(
        rawTodos,
        _searchQuery,
        _selectedCategory,
        _statusFilter,
        _sortBy
    ) { todosList, query, category, status, sort ->
        var filtered = todosList

        // Apply Search
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }

        // Apply Category
        if (category != "All") {
            filtered = filtered.filter { it.category == category }
        }

        // Apply Status
        when (status) {
            "Active" -> filtered = filtered.filter { !it.isCompleted }
            "Completed" -> filtered = filtered.filter { it.isCompleted }
        }

        // Apply Sort
        when (sort) {
            "Priority" -> {
                filtered = filtered.sortedWith(
                    compareBy<TodoItem> { it.isCompleted }
                        .thenByDescending { it.priority }
                        .thenByDescending { it.createdAt }
                )
            }
            "Alphabetical" -> {
                filtered = filtered.sortedWith(
                    compareBy<TodoItem> { it.isCompleted }
                        .thenBy { it.title.lowercase() }
                )
            }
            else -> { // "Date"
                filtered = filtered.sortedWith(
                    compareBy<TodoItem> { it.isCompleted }
                        .thenByDescending { it.createdAt }
                )
            }
        }

        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Categories available
    val categories = listOf("Personal", "Work", "Education", "Shopping", "Health", "Finance")

    // Stats
    val stats = rawTodos.map { list ->
        val total = list.size
        val completed = list.count { it.isCompleted }
        val active = total - completed
        val completionRate = if (total > 0) (completed.toFloat() / total * 100).toInt() else 0
        TodoStats(total, completed, active, completionRate)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodoStats()
    )

    // Operations
    fun addTodo(title: String, description: String, category: String, priority: Int, dueDate: Long?) {
        viewModelScope.launch {
            repository.insertTodo(
                TodoItem(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    dueDate = dueDate
                )
            )
        }
    }

    fun toggleTodo(todoItem: TodoItem) {
        viewModelScope.launch {
            repository.updateTodo(todoItem.copy(isCompleted = !todoItem.isCompleted))
        }
    }

    fun updateTodo(todoItem: TodoItem) {
        viewModelScope.launch {
            repository.updateTodo(todoItem)
        }
    }

    fun deleteTodo(todoItem: TodoItem) {
        viewModelScope.launch {
            repository.deleteTodo(todoItem)
        }
    }

    fun clearCompleted() {
        viewModelScope.launch {
            repository.clearCompletedTodos()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun setStatusFilter(status: String) {
        _statusFilter.value = status
    }

    fun setSortBy(sort: String) {
        _sortBy.value = sort
    }
}
