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

enum class ReminderFrequency(val title: String) {
    NONE("Без напоминания"),
    ONCE("Однократно"),
    DAILY("Ежедневно"),
    WEEKLY("Еженедельно"),
    MONTHLY("Ежемесячно");

    companion object {
        fun fromName(name: String?): ReminderFrequency = entries.firstOrNull { it.name == name } ?: NONE
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
        ),
        ForeignKey(
            entity = Assignee::class,
            parentColumns = ["id"],
            childColumns = ["assigneeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("projectId"), Index("assigneeId")]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val projectId: Long? = null,
    val assigneeId: Long? = null,
    val deadline: Long? = null,
    val status: TaskStatus = TaskStatus.NEW,
    val createdAt: Long = System.currentTimeMillis(),
    val deadlineNotified: Boolean = false,
    val reminderAt: Long? = null,
    val reminderFrequency: ReminderFrequency = ReminderFrequency.NONE
)
