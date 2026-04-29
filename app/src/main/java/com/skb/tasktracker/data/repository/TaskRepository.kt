package com.skb.tasktracker.data.repository

import com.skb.tasktracker.data.db.TaskDao
import com.skb.tasktracker.data.entity.Task
import com.skb.tasktracker.data.entity.TaskStatus
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {
    val tasks: Flow<List<Task>> = dao.observeAll()

    fun observe(id: Long): Flow<Task?> = dao.observeById(id)
    suspend fun get(id: Long): Task? = dao.getById(id)
    suspend fun save(task: Task): Long = if (task.id == 0L) dao.insert(task) else { dao.update(task); task.id }
    suspend fun delete(id: Long) = dao.deleteById(id)
    suspend fun setStatus(id: Long, status: TaskStatus) = dao.updateStatus(id, status)
    suspend fun upcomingDue(until: Long): List<Task> = dao.findUpcomingDue(until)
    suspend fun markNotified(id: Long) = dao.markDeadlineNotified(id)
}
