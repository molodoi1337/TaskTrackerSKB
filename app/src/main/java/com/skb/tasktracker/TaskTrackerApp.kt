package com.skb.tasktracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.skb.tasktracker.data.db.AppDatabase
import com.skb.tasktracker.data.repository.AssigneeRepository
import com.skb.tasktracker.data.repository.ProjectRepository
import com.skb.tasktracker.data.repository.TaskRepository
import com.skb.tasktracker.notifications.DeadlineWorker
import java.util.concurrent.TimeUnit

class TaskTrackerApp : Application() {
    val database by lazy { AppDatabase.get(this) }
    val taskRepository by lazy { TaskRepository(database.taskDao()) }
    val projectRepository by lazy { ProjectRepository(database.projectDao()) }
    val assigneeRepository by lazy { AssigneeRepository(database.assigneeDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleDeadlineWorker()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Уведомления задач",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Дедлайны и напоминания по задачам" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun scheduleDeadlineWorker() {
        val request = PeriodicWorkRequestBuilder<DeadlineWorker>(1, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            DeadlineWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    companion object {
        const val CHANNEL_ID = "task_deadline"
    }
}
