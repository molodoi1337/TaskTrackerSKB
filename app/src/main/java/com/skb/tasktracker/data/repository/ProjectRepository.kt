package com.skb.tasktracker.data.repository

import com.skb.tasktracker.data.db.ProjectDao
import com.skb.tasktracker.data.entity.Project
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val dao: ProjectDao) {
    val projects: Flow<List<Project>> = dao.observeAll()

    suspend fun get(id: Long): Project? = dao.getById(id)
    suspend fun save(project: Project): Long = if (project.id == 0L) dao.insert(project) else { dao.update(project); project.id }
    suspend fun delete(id: Long) = dao.deleteById(id)
}
