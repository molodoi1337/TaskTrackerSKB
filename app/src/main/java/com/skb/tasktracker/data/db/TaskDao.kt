package com.skb.tasktracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.skb.tasktracker.data.entity.Task
import com.skb.tasktracker.data.entity.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeById(id: Long): Flow<Task?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE tasks SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: TaskStatus)

    @Query("UPDATE tasks SET deadlineNotified = 1 WHERE id = :id")
    suspend fun markDeadlineNotified(id: Long)

    @Query("SELECT * FROM tasks WHERE deadline IS NOT NULL AND deadline <= :until AND status != 'DONE' AND deadlineNotified = 0")
    suspend fun findUpcomingDue(until: Long): List<Task>
}
