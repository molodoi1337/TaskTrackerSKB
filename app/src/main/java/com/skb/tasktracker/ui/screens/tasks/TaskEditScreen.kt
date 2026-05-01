package com.skb.tasktracker.ui.screens.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skb.tasktracker.TaskTrackerApp
import com.skb.tasktracker.data.entity.ReminderFrequency
import com.skb.tasktracker.data.entity.TaskStatus
import com.skb.tasktracker.ui.components.appViewModel
import com.skb.tasktracker.util.formatDate
import com.skb.tasktracker.util.formatDateTime
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditScreen(
    taskId: Long?,
    onBack: () -> Unit,
    onAddAssignee: () -> Unit
) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as TaskTrackerApp
    val vm = appViewModel(key = "edit-${taskId ?: "new"}") {
        TaskEditViewModel(app.taskRepository, app.projectRepository, app.assigneeRepository, app, taskId)
    }
    val state by vm.state.collectAsStateWithLifecycle()
    val projects by vm.projects.collectAsStateWithLifecycle()
    val assignees by vm.assignees.collectAsStateWithLifecycle()
    var showDeadlinePicker by remember { mutableStateOf(false) }
    var showReminderDate by remember { mutableStateOf(false) }
    var showReminderTime by remember { mutableStateOf(false) }
    var pendingReminderDate by remember { mutableStateOf<Long?>(null) }
    var projectExpanded by remember { mutableStateOf(false) }
    var assigneeExpanded by remember { mutableStateOf(false) }
    var frequencyExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) { if (state.saved) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == null) "Новая задача" else "Редактирование") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = vm::setTitle,
                label = { Text("Название*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.error != null
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = vm::setDescription,
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = assigneeExpanded,
                    onExpandedChange = { assigneeExpanded = !assigneeExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    val aname = assignees.firstOrNull { it.id == state.assigneeId }?.name
                        ?: "— Не назначен —"
                    OutlinedTextField(
                        value = aname,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Исполнитель") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(assigneeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = assigneeExpanded,
                        onDismissRequest = { assigneeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("— Не назначен —") },
                            onClick = { vm.setAssignee(null); assigneeExpanded = false }
                        )
                        assignees.forEach { a ->
                            DropdownMenuItem(
                                text = {
                                    Text(if (a.role.isBlank()) a.name else "${a.name} · ${a.role}")
                                },
                                onClick = { vm.setAssignee(a.id); assigneeExpanded = false }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("+ Добавить исполнителя") },
                            onClick = { assigneeExpanded = false; onAddAssignee() }
                        )
                    }
                }
                IconButton(onClick = onAddAssignee) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить исполнителя")
                }
            }

            ExposedDropdownMenuBox(
                expanded = projectExpanded,
                onExpandedChange = { projectExpanded = !projectExpanded }
            ) {
                val pname = projects.firstOrNull { it.id == state.projectId }?.name ?: "— Без проекта —"
                OutlinedTextField(
                    value = pname,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Проект") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(projectExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = projectExpanded,
                    onDismissRequest = { projectExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("— Без проекта —") },
                        onClick = { vm.setProject(null); projectExpanded = false }
                    )
                    projects.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.name) },
                            onClick = { vm.setProject(p.id); projectExpanded = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.deadline.formatDate(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Дедлайн") },
                trailingIcon = {
                    IconButton(onClick = { showDeadlinePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Напоминание", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = state.reminderAt.formatDateTime(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Когда напомнить") },
                trailingIcon = {
                    Row {
                        if (state.reminderAt != null) {
                            IconButton(onClick = { vm.setReminderAt(null) }) {
                                Icon(Icons.Default.Clear, contentDescription = "Очистить")
                            }
                        }
                        IconButton(onClick = { showReminderDate = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Выбрать дату")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = frequencyExpanded,
                onExpandedChange = { frequencyExpanded = !frequencyExpanded }
            ) {
                OutlinedTextField(
                    value = state.reminderFrequency.title,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Частота") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(frequencyExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = frequencyExpanded,
                    onDismissRequest = { frequencyExpanded = false }
                ) {
                    ReminderFrequency.entries.forEach { f ->
                        DropdownMenuItem(
                            text = { Text(f.title) },
                            onClick = {
                                vm.setReminderFrequency(f); frequencyExpanded = false
                            }
                        )
                    }
                }
            }

            Text("Статус", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TaskStatus.entries.forEach { s ->
                    FilterChip(
                        selected = state.status == s,
                        onClick = { vm.setStatus(s) },
                        label = { Text(s.title) }
                    )
                }
            }

            Button(
                onClick = vm::save,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(if (taskId == null) "Создать" else "Сохранить")
            }
        }
    }

    if (showDeadlinePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = state.deadline)
        DatePickerDialog(
            onDismissRequest = { showDeadlinePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    vm.setDeadline(dpState.selectedDateMillis)
                    showDeadlinePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    vm.setDeadline(null)
                    showDeadlinePicker = false
                }) { Text("Очистить") }
            }
        ) { DatePicker(state = dpState) }
    }

    if (showReminderDate) {
        val initial = state.reminderAt ?: System.currentTimeMillis()
        val dpState = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { showReminderDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pendingReminderDate = dpState.selectedDateMillis
                    showReminderDate = false
                    if (pendingReminderDate != null) showReminderTime = true
                }) { Text("Далее") }
            },
            dismissButton = {
                TextButton(onClick = {
                    vm.setReminderAt(null)
                    pendingReminderDate = null
                    showReminderDate = false
                }) { Text("Очистить") }
            }
        ) { DatePicker(state = dpState) }
    }

    if (showReminderTime) {
        val baseMillis = pendingReminderDate ?: state.reminderAt ?: System.currentTimeMillis()
        val cal = remember(baseMillis) { Calendar.getInstance().apply { timeInMillis = baseMillis } }
        val tpState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showReminderTime = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = pendingReminderDate ?: state.reminderAt ?: System.currentTimeMillis()
                    val combined = Calendar.getInstance().apply {
                        timeInMillis = date
                        set(Calendar.HOUR_OF_DAY, tpState.hour)
                        set(Calendar.MINUTE, tpState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    vm.setReminderAt(combined)
                    pendingReminderDate = null
                    showReminderTime = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingReminderDate = null
                    showReminderTime = false
                }) { Text("Отмена") }
            },
            title = { Text("Время напоминания") },
            text = { TimePicker(state = tpState) }
        )
    }

    state.error?.let {
        AlertDialog(
            onDismissRequest = vm::clearError,
            confirmButton = { TextButton(onClick = vm::clearError) { Text("OK") } },
            text = { Text(it) }
        )
    }
}
