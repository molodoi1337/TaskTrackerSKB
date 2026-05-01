package com.skb.tasktracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.skb.tasktracker.data.entity.Assignee
import com.skb.tasktracker.data.entity.Project
import com.skb.tasktracker.data.entity.Task

@Database(
    entities = [Task::class, Project::class, Assignee::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun assigneeDao(): AssigneeDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "task_tracker.db"
            ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }
    }
}
