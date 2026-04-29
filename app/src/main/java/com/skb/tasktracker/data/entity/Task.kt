package com.skb.tasktracker.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TaskStatus(val title: String) {
    NEW("Новая"),
    IN_PROGRESS("В работе"),
    DONE("Выполнена");

    companion object {
        fun fromName(name: String?): TaskStatus = entries.firstOrNull { it.name == name } ?: NEW
    }
}

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("projectId")]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val projectId: Long? = null,
    val assignee: String = "",
    val deadline: Long? = null,
    val status: TaskStatus = TaskStatus.NEW,
    val createdAt: Long = System.currentTimeMillis(),
    val deadlineNotified: Boolean = false
)
