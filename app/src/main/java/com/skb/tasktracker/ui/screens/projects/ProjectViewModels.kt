package com.skb.tasktracker.ui.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skb.tasktracker.data.entity.Project
import com.skb.tasktracker.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProjectListViewModel(
    private val repo: ProjectRepository
) : ViewModel() {
    val projects: StateFlow<List<Project>> = repo.projects.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    fun delete(id: Long) {
        viewModelScope.launch { repo.delete(id) }
    }
}

data class ProjectEditState(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val saved: Boolean = false,
    val error: String? = null
)

class ProjectEditViewModel(
    private val repo: ProjectRepository,
    initialId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectEditState())
    val state: StateFlow<ProjectEditState> = _state.asStateFlow()

    init {
        if (initialId != null) {
            viewModelScope.launch {
                repo.get(initialId)?.let { p ->
                    _state.value = ProjectEditState(id = p.id, name = p.name, description = p.description)
                }
            }
        }
    }

    fun setName(v: String) = _state.update { it.copy(name = v) }
    fun setDescription(v: String) = _state.update { it.copy(description = v) }
    fun clearError() = _state.update { it.copy(error = null) }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.update { it.copy(error = "Название проекта обязательно") }
            return
        }
        viewModelScope.launch {
            repo.save(Project(id = s.id, name = s.name.trim(), description = s.description.trim()))
            _state.update { it.copy(saved = true) }
        }
    }
}
