package com.skb.tasktracker.ui.screens.assignees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skb.tasktracker.data.entity.Assignee
import com.skb.tasktracker.data.repository.AssigneeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AssigneeListViewModel(
    private val repo: AssigneeRepository
) : ViewModel() {
    val assignees: StateFlow<List<Assignee>> = repo.assignees.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    fun delete(id: Long) {
        viewModelScope.launch { repo.delete(id) }
    }
}

data class AssigneeEditState(
    val id: Long = 0,
    val name: String = "",
    val role: String = "",
    val saved: Boolean = false,
    val error: String? = null
)

class AssigneeEditViewModel(
    private val repo: AssigneeRepository,
    initialId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(AssigneeEditState())
    val state: StateFlow<AssigneeEditState> = _state.asStateFlow()

    init {
        if (initialId != null) {
            viewModelScope.launch {
                repo.get(initialId)?.let { a ->
                    _state.value = AssigneeEditState(id = a.id, name = a.name, role = a.role)
                }
            }
        }
    }

    fun setName(v: String) = _state.update { it.copy(name = v) }
    fun setRole(v: String) = _state.update { it.copy(role = v) }
    fun clearError() = _state.update { it.copy(error = null) }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.update { it.copy(error = "Имя исполнителя обязательно") }
            return
        }
        viewModelScope.launch {
            repo.save(Assignee(id = s.id, name = s.name.trim(), role = s.role.trim()))
            _state.update { it.copy(saved = true) }
        }
    }
}
