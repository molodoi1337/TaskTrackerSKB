package com.skb.tasktracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.skb.tasktracker.data.entity.Assignee
import kotlinx.coroutines.flow.Flow

@Dao
interface AssigneeDao {
    @Query("SELECT * FROM assignees ORDER BY name ASC")
    fun observeAll(): Flow<List<Assignee>>

    @Query("SELECT * FROM assignees WHERE id = :id")
    suspend fun getById(id: Long): Assignee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assignee: Assignee): Long

    @Update
    suspend fun update(assignee: Assignee)

    @Query("DELETE FROM assignees WHERE id = :id")
    suspend fun deleteById(id: Long)
}
