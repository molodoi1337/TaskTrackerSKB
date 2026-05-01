package com.skb.tasktracker.data.repository

import com.skb.tasktracker.data.db.AssigneeDao
import com.skb.tasktracker.data.entity.Assignee
import kotlinx.coroutines.flow.Flow

class AssigneeRepository(private val dao: AssigneeDao) {
    val assignees: Flow<List<Assignee>> = dao.observeAll()

    suspend fun get(id: Long): Assignee? = dao.getById(id)
    suspend fun save(assignee: Assignee): Long =
        if (assignee.id == 0L) dao.insert(assignee) else { dao.update(assignee); assignee.id }
    suspend fun delete(id: Long) = dao.deleteById(id)
}
