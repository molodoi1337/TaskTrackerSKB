package com.skb.tasktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assignees")
data class Assignee(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
