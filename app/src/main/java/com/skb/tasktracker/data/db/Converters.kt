package com.skb.tasktracker.data.db

import androidx.room.TypeConverter
import com.skb.tasktracker.data.entity.TaskStatus

class Converters {
    @TypeConverter
    fun statusToString(status: TaskStatus): String = status.name

    @TypeConverter
    fun stringToStatus(value: String): TaskStatus = TaskStatus.fromName(value)
}
