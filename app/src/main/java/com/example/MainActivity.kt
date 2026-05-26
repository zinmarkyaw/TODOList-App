package com.example

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TodoItem
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: TodoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
            MyApplicationTheme(themeOption = currentTheme) {
                TodoAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoAppScreen(viewModel: TodoViewModel) {
    val todos by viewModel.todos.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf<TodoItem?>(null) } // Non-null if editing or adding
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("todo_app_scaffold"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddEditDialog = TodoItem(title = "") // Empty item indicates creation
                    isEditing = false
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("add_todo_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Clean Minimalism Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 12.dp, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "My Tasks",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Normal,
                                letterSpacing = (-0.5).sp,
                                fontSize = 32.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${stats.completed} of ${stats.total} completed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Action to clear completed
                        if (stats.completed > 0) {
                            TextButton(
                                onClick = { viewModel.clearCompleted() },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.testTag("clear_completed_button")
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear Done", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // Theme Switcher Button with Dropdown
                        val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
                        var themeMenuExpanded by remember { mutableStateOf(false) }

                        Box {
                            IconButton(
                                onClick = { themeMenuExpanded = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    .testTag("theme_switcher_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Switch Theme",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = themeMenuExpanded,
                                onDismissRequest = { themeMenuExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                Text(
                                    text = "Select Palette",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                com.example.ui.theme.AppThemeOption.values().forEach { option ->
                                    val isCurrent = selectedTheme == option
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Tiny color dot preview
                                                val dotColor = when (option) {
                                                    com.example.ui.theme.AppThemeOption.LAVENDER -> Color(0xFF6750A4)
                                                    com.example.ui.theme.AppThemeOption.SLATE -> Color(0xFF6550A4)
                                                    com.example.ui.theme.AppThemeOption.FOREST -> Color(0xFF2E6C4C)
                                                    com.example.ui.theme.AppThemeOption.COZY -> Color(0xFF855300)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(dotColor)
                                                )
                                                Text(
                                                    text = when (option) {
                                                        com.example.ui.theme.AppThemeOption.LAVENDER -> "Lavender Minimal"
                                                        com.example.ui.theme.AppThemeOption.SLATE -> "Slate Dark"
                                                        com.example.ui.theme.AppThemeOption.FOREST -> "Forest Mint"
                                                        com.example.ui.theme.AppThemeOption.COZY -> "Cozy Amber"
                                                    },
                                                    style = MaterialTheme.typography.labelLarge.copy(
                                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.setSelectedTheme(option)
                                            themeMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // User Initials Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AZ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                if (stats.total > 0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { stats.completed.toFloat() / stats.total },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                }
            }

            // Search and Filters Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Search Input Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input"),
                    placeholder = { Text("Search tasks...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.setSearchQuery("") },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Category Row Filter
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterCategoryChip(
                            category = "All",
                            isSelected = selectedCategory == "All",
                            onClick = { viewModel.setCategoryFilter("All") }
                        )
                    }
                    items(viewModel.categories) { cat ->
                        FilterCategoryChip(
                            category = cat,
                            isSelected = selectedCategory == cat,
                            onClick = { viewModel.setCategoryFilter(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status and Sort row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status filters (All, Active, Completed) using segmented look
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(2.dp)
                    ) {
                        listOf("All", "Active", "Completed").forEach { status ->
                            val isSelected = statusFilter == status
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                                    .clickable { viewModel.setStatusFilter(status) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = status,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Sort By Dropdown selection
                    var sortExpanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(
                            onClick = { sortExpanded = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("sort_button")
                        ) {
                            Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sort: $sortBy", style = MaterialTheme.typography.labelLarge)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = sortExpanded,
                            onDismissRequest = { sortExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            listOf("Date", "Priority", "Alphabetical").forEach { sortOption ->
                                DropdownMenuItem(
                                    text = { Text(sortOption) },
                                    onClick = {
                                        viewModel.setSortBy(sortOption)
                                        sortExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Task list showing empty state or regular scroll list
            if (todos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching tasks" else "All tasks done!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try adjusting your search filters" else "Tap + below to schedule a new task",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("todo_items_list"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(todos, key = { it.id }) { todo ->
                        TodoRowItem(
                            todo = todo,
                            onToggle = { viewModel.toggleTodo(todo) },
                            onEdit = {
                                showAddEditDialog = todo
                                isEditing = true
                            },
                            onDelete = { viewModel.deleteTodo(todo) }
                        )
                    }
                }
            }
        }
    }

    // Modal creation and edit dialog
    showAddEditDialog?.let { currentTodo ->
        AddEditTodoDialog(
            todo = currentTodo,
            isEditing = isEditing,
            categories = viewModel.categories,
            onDismiss = { showAddEditDialog = null },
            onSave = { title, desc, cat, priority, dueDate ->
                if (isEditing) {
                    viewModel.updateTodo(
                        currentTodo.copy(
                            title = title,
                            description = desc,
                            category = cat,
                            priority = priority,
                            dueDate = dueDate
                        )
                    )
                } else {
                    viewModel.addTodo(
                        title = title,
                        description = desc,
                        category = cat,
                        priority = priority,
                        dueDate = dueDate
                    )
                }
                showAddEditDialog = null
            }
        )
    }
}

@Composable
fun FilterCategoryChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(36.dp)
            .testTag("category_chip_$category")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val icon = when (category) {
                "Personal" -> Icons.Default.Home
                "Work" -> Icons.Default.Info
                "Education" -> Icons.Default.List
                "Shopping" -> Icons.Default.ShoppingCart
                "Health" -> Icons.Default.Warning
                "Finance" -> Icons.Default.Favorite
                else -> Icons.Default.Star
            }
            if (category != "All") {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(category, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun TodoRowItem(
    todo: TodoItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val strikeThroughTransition = remember { Animatable(0f) }
    
    LaunchedEffect(todo.isCompleted) {
        strikeThroughTransition.animateTo(
            targetValue = if (todo.isCompleted) 1f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )
    }

    val scale by animateFloatAsState(
        targetValue = if (todo.isCompleted) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("todo_item_card_${todo.id}")
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(24.dp),
        border = if (todo.isCompleted) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = if (todo.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant
            else
                Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High-fidelity custom minimalist check box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onToggle)
                    .testTag("todo_checkbox_container_${todo.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (todo.isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Task Completed",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Transparent)
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Task Title
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null
                        ),
                        color = if (todo.isCompleted)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        else
                            MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Priority Badge
                    val (priorityLabel, priorityColor) = when (todo.priority) {
                        2 -> "High" to MaterialTheme.colorScheme.error
                        1 -> "Medium" to MaterialTheme.colorScheme.primary
                        else -> "Low" to MaterialTheme.colorScheme.secondary
                    }
                    if (!todo.isCompleted) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(priorityColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = priorityLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = priorityColor
                            )
                        }
                    }
                }

                if (todo.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todo.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (todo.isCompleted)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Meta Info Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Emblem
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = todo.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Due Date Info
                    todo.dueDate?.let { due ->
                        val now = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        val isOverdue = due < now && !todo.isCompleted
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Due Date icon",
                                tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = formatLongToDate(due),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Edit and Delete Actions
            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("todo_edit_button_${todo.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Task",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("todo_delete_button_${todo.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Task",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditTodoDialog(
    todo: TodoItem,
    isEditing: Boolean,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, category: String, priority: Int, dueDate: Long?) -> Unit
) {
    var title by remember { mutableStateOf(todo.title) }
    var description by remember { mutableStateOf(todo.description) }
    var category by remember { mutableStateOf(todo.category) }
    var priority by remember { mutableStateOf(todo.priority) } // 0 = Low, 1 = Medium, 2 = High
    var dueDate by remember { mutableStateOf<Long?>(todo.dueDate) }

    val context = LocalContext.current
    var titleError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentHeight(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isEditing) "Modify Task" else "Plan New Task",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Task Title *") },
                    isError = titleError,
                    supportingText = {
                        if (titleError) {
                            Text("Title is required", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("todo_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    )
                )

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("todo_desc_input"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )

                // Priority Row Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Impact Priority", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Low" to 0, "Medium" to 1, "High" to 2).forEach { (label, value) ->
                            val isSelected = priority == value
                            val badgeColor = when (value) {
                                2 -> MaterialTheme.colorScheme.error
                                1 -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.secondary
                            }
                            
                            Surface(
                                color = if (isSelected) badgeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { priority = value }
                                    .height(40.dp)
                                    .testTag("priority_selection_$label")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }

                // Category Spinner/Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Category", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSelected = category == cat
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .clickable { category = cat }
                                    .height(36.dp)
                                    .testTag("dialog_category_$cat")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                ) {
                                    Text(cat, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }

                // Date Picker Action Area
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Task Deadline", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Surface(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            if (dueDate != null) {
                                calendar.timeInMillis = dueDate!!
                            }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        set(Calendar.HOUR_OF_DAY, 23)
                                        set(Calendar.MINUTE, 59)
                                        set(Calendar.SECOND, 59)
                                    }
                                    dueDate = selectedCal.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("date_picker_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (dueDate == null) "Schedule target date" else formatLongToDate(dueDate!!),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (dueDate == null) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (dueDate != null) {
                                IconButton(
                                    onClick = { dueDate = null },
                                    modifier = Modifier.minimumInteractiveComponentSize().testTag("clear_date_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear selected date",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom actions buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("dialog_cancel_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                            } else {
                                onSave(title.trim(), description.trim(), category, priority, dueDate)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("dialog_save_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Schedule")
                    }
                }
            }
        }
    }
}

fun formatLongToDate(timeInMillis: Long): String {
    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return format.format(Date(timeInMillis))
}
