package com.skb.tasktracker.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skb.tasktracker.data.entity.Task
import com.skb.tasktracker.data.entity.TaskStatus
import com.skb.tasktracker.ui.components.StatusChip
import com.skb.tasktracker.ui.components.appViewModel
import com.skb.tasktracker.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onAddTask: () -> Unit,
    onOpenTask: (Long) -> Unit,
    onOpenProjects: () -> Unit
) {
    val vm = appViewModel { app -> TaskListViewModel(app.taskRepository, app.projectRepository) }
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val filters by vm.filters.collectAsStateWithLifecycle()
    val projects by vm.projects.collectAsStateWithLifecycle()
    var sortMenu by remember { mutableStateOf(false) }
    var projectMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Tracker SKB", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = onOpenProjects) {
                        Icon(Icons.Default.Folder, contentDescription = "Проекты")
                    }
                    Box {
                        IconButton(onClick = { sortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Сортировка")
                        }
                        DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                            SortBy.entries.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.title) },
                                    onClick = { vm.setSort(s); sortMenu = false }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = filters.query,
                onValueChange = vm::setQuery,
                placeholder = { Text("Поиск задач") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filters.statusFilter == null,
                    onClick = { vm.setStatusFilter(null) },
                    label = { Text("Все") }
                )
                TaskStatus.entries.forEach { s ->
                    FilterChip(
                        selected = filters.statusFilter == s,
                        onClick = { vm.setStatusFilter(if (filters.statusFilter == s) null else s) },
                        label = { Text(s.title) }
                    )
                }
            }
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                FilterChip(
                    selected = filters.projectFilter != null,
                    onClick = { projectMenu = true },
                    label = {
                        val pname = projects.firstOrNull { it.id == filters.projectFilter }?.name
                        Text(pname ?: "Все проекты")
                    },
                    leadingIcon = { Icon(Icons.Default.Folder, null, modifier = Modifier.size(18.dp)) }
                )
                DropdownMenu(expanded = projectMenu, onDismissRequest = { projectMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Все проекты") },
                        onClick = { vm.setProjectFilter(null); projectMenu = false }
                    )
                    projects.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.name) },
                            onClick = { vm.setProjectFilter(p.id); projectMenu = false }
                        )
                    }
                }
            }
            if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Задач пока нет.\nНажмите +, чтобы создать первую.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            projectName = projects.firstOrNull { it.id == task.projectId }?.name,
                            onClick = { onOpenTask(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCard(task: Task, projectName: String?, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(task.status)
            }
            if (task.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }
            Spacer(Modifier.height(10.dp))
            Row {
                projectName?.let {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(8.dp))
                }
                if (task.assignee.isNotBlank()) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(task.assignee, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.width(8.dp))
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(2.dp))
                Text(task.deadline.formatDate(), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
