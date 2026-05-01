package com.skb.tasktracker.ui.screens.tasks

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skb.tasktracker.data.entity.Assignee
import com.skb.tasktracker.data.entity.Project
import com.skb.tasktracker.data.entity.ReminderFrequency
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskEditState(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val projectId: Long? = null,
    val assigneeId: Long? = null,
    val deadline: Long? = null,
    val status: TaskStatus = TaskStatus.NEW,
    val reminderAt: Long? = null,
    val reminderFrequency: ReminderFrequency = ReminderFrequency.NONE,
    val loading: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

class TaskEditViewModel(
    private val taskRepo: TaskRepository,
    projectRepo: ProjectRepository,
    assigneeRepo: AssigneeRepository,
    private val appContext: Context,
    initialId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(TaskEditState(loading = initialId != null))
    val state: StateFlow<TaskEditState> = _state.asStateFlow()

    val projects: StateFlow<List<Project>> = projectRepo.projects.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    val assignees: StateFlow<List<Assignee>> = assigneeRepo.assignees.stateIn(
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
                        assigneeId = t.assigneeId,
                        deadline = t.deadline,
                        status = t.status,
                        reminderAt = t.reminderAt,
                        reminderFrequency = t.reminderFrequency
                    )
                } ?: run { _state.update { it.copy(loading = false) } }
            }
        }
    }

    fun setTitle(v: String) = _state.update { it.copy(title = v) }
    fun setDescription(v: String) = _state.update { it.copy(description = v) }
    fun setProject(id: Long?) = _state.update { it.copy(projectId = id) }
    fun setAssignee(id: Long?) = _state.update { it.copy(assigneeId = id) }
    fun setDeadline(v: Long?) = _state.update { it.copy(deadline = v) }
    fun setStatus(s: TaskStatus) = _state.update { it.copy(status = s) }
    fun setReminderAt(v: Long?) = _state.update {
        val frequency = if (v == null) ReminderFrequency.NONE
        else if (it.reminderFrequency == ReminderFrequency.NONE) ReminderFrequency.ONCE
        else it.reminderFrequency
        it.copy(reminderAt = v, reminderFrequency = frequency)
    }
    fun setReminderFrequency(f: ReminderFrequency) = _state.update {
        if (f == ReminderFrequency.NONE) it.copy(reminderFrequency = f, reminderAt = null)
        else it.copy(reminderFrequency = f)
    }

    fun save() {
        val s = _state.value
        if (s.title.isBlank()) {
            _state.update { it.copy(error = "Название задачи обязательно") }
            return
        }
        if (s.reminderFrequency != ReminderFrequency.NONE && s.reminderAt == null) {
            _state.update { it.copy(error = "Укажите дату и время напоминания") }
            return
        }
        viewModelScope.launch {
            val task = Task(
                id = s.id,
                title = s.title.trim(),
                description = s.description.trim(),
                projectId = s.projectId,
                assigneeId = s.assigneeId,
                deadline = s.deadline,
                status = s.status,
                deadlineNotified = false,
                reminderAt = s.reminderAt,
                reminderFrequency = s.reminderFrequency
            )
            val savedId = taskRepo.save(task)
            if (s.reminderFrequency != ReminderFrequency.NONE && s.reminderAt != null) {
                ReminderScheduler.schedule(appContext, savedId, s.reminderAt)
            } else {
                ReminderScheduler.cancel(appContext, savedId)
            }
            _state.update { it.copy(saved = true) }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
