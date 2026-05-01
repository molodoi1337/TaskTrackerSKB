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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val taskRepo: TaskRepository,
    private val projectRepo: ProjectRepository,
    private val assigneeRepo: AssigneeRepository,
    private val appContext: Context,
    private val taskId: Long
) : ViewModel() {

    val task: StateFlow<Task?> = taskRepo.observe(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val project: StateFlow<Project?> = taskRepo.observe(taskId)
        .map { it?.projectId?.let { id -> projectRepo.get(id) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val assignee: StateFlow<Assignee?> = taskRepo.observe(taskId)
        .map { it?.assigneeId?.let { id -> assigneeRepo.get(id) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun changeStatus(status: TaskStatus) {
        viewModelScope.launch { taskRepo.setStatus(taskId, status) }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            ReminderScheduler.cancel(appContext, taskId)
            taskRepo.delete(taskId)
            onDone()
        }
    }
}
