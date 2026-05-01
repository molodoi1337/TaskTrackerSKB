package com.skb.tasktracker.ui.navigation

sealed class Screen(val route: String) {
    data object TaskList : Screen("tasks")
    data object Projects : Screen("projects")
    data object Assignees : Screen("assignees")
    data object TaskEdit : Screen("task_edit?id={id}") {
        fun build(id: Long? = null) = "task_edit?id=${id ?: -1}"
    }
    data object TaskDetail : Screen("task_detail/{id}") {
        fun build(id: Long) = "task_detail/$id"
    }
    data object ProjectEdit : Screen("project_edit?id={id}") {
        fun build(id: Long? = null) = "project_edit?id=${id ?: -1}"
    }
    data object AssigneeEdit : Screen("assignee_edit?id={id}") {
        fun build(id: Long? = null) = "assignee_edit?id=${id ?: -1}"
    }
}
