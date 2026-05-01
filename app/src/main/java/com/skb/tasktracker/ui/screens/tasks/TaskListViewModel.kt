package com.skb.tasktracker.ui.screens.tasks

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skb.tasktracker.data.entity.Assignee
import com.skb.tasktracker.data.entity.Project
import com.skb.tasktracker.data.entity.Task
import com.skb.tasktracker.data.entity.TaskStatus
import com.skb.tasktracker.data.repository.AssigneeRepository
import com.skb.tasktracker.data.repository.ProjectRepository
import com.skb.tasktracker.data.repository.TaskRepository
import com.skb.tasktracker.notifications.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SortBy(val title: String) {
    CREATED("По дате создания"),
    DEADLINE("По дедлайну"),
    STATUS("По статусу")
}

data class TaskListUiState(
    val query: String = "",
    val statusFilter: TaskStatus? = null,
    val projectFilter: Long? = null,
    val sortBy: SortBy = SortBy.CREATED
)

class TaskListViewModel(
    private val taskRepo: TaskRepository,
    projectRepo: ProjectRepository,
    assigneeRepo: AssigneeRepository,
    private val appContext: Context
) : ViewModel() {

    private val _filters = MutableStateFlow(TaskListUiState())
    val filters: StateFlow<TaskListUiState> = _filters.asStateFlow()

    val projects: StateFlow<List<Project>> = projectRepo.projects.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    val assignees: StateFlow<List<Assignee>> = assigneeRepo.assignees.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    val tasks: StateFlow<List<Task>> = combine(taskRepo.tasks, _filters) { all, f ->
        var result = all
        if (f.query.isNotBlank()) {
            val q = f.query.trim().lowercase()
            result = result.filter { it.title.lowercase().contains(q) || it.description.lowercase().contains(q) }
        }
        f.statusFilter?.let { s -> result = result.filter { it.status == s } }
        f.projectFilter?.let { p -> result = result.filter { it.projectId == p } }
        result = when (f.sortBy) {
            SortBy.CREATED -> result.sortedByDescending { it.createdAt }
            SortBy.DEADLINE -> result.sortedBy { it.deadline ?: Long.MAX_VALUE }
            SortBy.STATUS -> result.sortedBy { it.status.ordinal }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) = _filters.update { it.copy(query = value) }
    fun setStatusFilter(status: TaskStatus?) = _filters.update { it.copy(statusFilter = status) }
    fun setProjectFilter(id: Long?) = _filters.update { it.copy(projectFilter = id) }
    fun setSort(sort: SortBy) = _filters.update { it.copy(sortBy = sort) }

    fun changeStatus(task: Task, status: TaskStatus) {
        viewModelScope.launch { taskRepo.setStatus(task.id, status) }
    }

    fun delete(task: Task) {
        viewModelScope.launch {
            ReminderScheduler.cancel(appContext, task.id)
            taskRepo.delete(task.id)
        }
    }
}
