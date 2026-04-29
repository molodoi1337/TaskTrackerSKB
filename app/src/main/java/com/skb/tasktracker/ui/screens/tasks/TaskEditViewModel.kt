package com.skb.tasktracker.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skb.tasktracker.data.entity.Project
import com.skb.tasktracker.data.entity.Task
import com.skb.tasktracker.data.entity.TaskStatus
import com.skb.tasktracker.data.repository.ProjectRepository
import com.skb.tasktracker.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskEditState(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val projectId: Long? = null,
    val assignee: String = "",
    val deadline: Long? = null,
    val status: TaskStatus = TaskStatus.NEW,
    val loading: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

class TaskEditViewModel(
    private val taskRepo: TaskRepository,
    projectRepo: ProjectRepository,
    initialId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(TaskEditState(loading = initialId != null))
    val state: StateFlow<TaskEditState> = _state.asStateFlow()

    val projects: StateFlow<List<Project>> = projectRepo.projects.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    init {
        if (initialId != null) {
            viewModelScope.launch {
                taskRepo.get(initialId)?.let { t ->
                    _state.value = TaskEditState(
                        id = t.id,
                        title = t.title,
                        description = t.description,
                        projectId = t.projectId,
                        assignee = t.assignee,
                        deadline = t.deadline,
                        status = t.status
                    )
                } ?: run { _state.update { it.copy(loading = false) } }
            }
        }
    }

    fun setTitle(v: String) = _state.update { it.copy(title = v) }
    fun setDescription(v: String) = _state.update { it.copy(description = v) }
    fun setProject(id: Long?) = _state.update { it.copy(projectId = id) }
    fun setAssignee(v: String) = _state.update { it.copy(assignee = v) }
    fun setDeadline(v: Long?) = _state.update { it.copy(deadline = v) }
    fun setStatus(s: TaskStatus) = _state.update { it.copy(status = s) }

    fun save() {
        val s = _state.value
        if (s.title.isBlank()) {
            _state.update { it.copy(error = "Название задачи обязательно") }
            return
        }
        viewModelScope.launch {
            val task = Task(
                id = s.id,
                title = s.title.trim(),
                description = s.description.trim(),
                projectId = s.projectId,
                assignee = s.assignee.trim(),
                deadline = s.deadline,
                status = s.status,
                deadlineNotified = false
            )
            taskRepo.save(task)
            _state.update { it.copy(saved = true) }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
