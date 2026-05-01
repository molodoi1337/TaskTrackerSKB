package com.skb.tasktracker.ui.screens.assignees

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skb.tasktracker.ui.components.appViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssigneeEditScreen(
    assigneeId: Long?,
    onBack: () -> Unit
) {
    val vm = appViewModel(key = "assignee-${assigneeId ?: "new"}") { app ->
        AssigneeEditViewModel(app.assigneeRepository, assigneeId)
    }
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) { if (state.saved) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (assigneeId == null) "Новый исполнитель" else "Редактирование") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = vm::setName,
                label = { Text("Имя*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.error != null
            )
            OutlinedTextField(
                value = state.role,
                onValueChange = vm::setRole,
                label = { Text("Должность / роль") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(onClick = vm::save, modifier = Modifier.fillMaxWidth()) {
                Text(if (assigneeId == null) "Создать" else "Сохранить")
            }
        }
    }

    state.error?.let {
        AlertDialog(
            onDismissRequest = vm::clearError,
            confirmButton = { TextButton(onClick = vm::clearError) { Text("OK") } },
            text = { Text(it) }
        )
    }
}
