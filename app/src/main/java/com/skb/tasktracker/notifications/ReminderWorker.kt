package com.skb.tasktracker.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.skb.tasktracker.MainActivity
import com.skb.tasktracker.R
import com.skb.tasktracker.TaskTrackerApp
import com.skb.tasktracker.data.entity.ReminderFrequency
import com.skb.tasktracker.data.entity.TaskStatus
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(KEY_TASK_ID, -1L)
        if (taskId < 0) return Result.success()

        val app = applicationContext as TaskTrackerApp
        val task = app.taskRepository.get(taskId) ?: return Result.success()
        if (task.status == TaskStatus.DONE) return Result.success()
        if (task.reminderFrequency == ReminderFrequency.NONE) return Result.success()

        if (hasNotificationPermission()) {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pi = PendingIntent.getActivity(
                applicationContext,
                (task.id * 31 + 1).toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(applicationContext, TaskTrackerApp.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Напоминание: ${task.title}")
                .setContentText(
                    if (task.description.isNotBlank()) task.description
                    else "Запланированное напоминание"
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(applicationContext)
                .notify((task.id * 31 + 1).toInt(), notification)
        }

        val current = task.reminderAt ?: System.currentTimeMillis()
        val nextAt = when (task.reminderFrequency) {
            ReminderFrequency.DAILY -> current + TimeUnit.DAYS.toMillis(1)
            ReminderFrequency.WEEKLY -> current + TimeUnit.DAYS.toMillis(7)
            ReminderFrequency.MONTHLY -> Calendar.getInstance().apply {
                timeInMillis = current
                add(Calendar.MONTH, 1)
            }.timeInMillis
            ReminderFrequency.ONCE, ReminderFrequency.NONE -> null
        }

        if (nextAt != null) {
            app.taskRepository.save(task.copy(reminderAt = nextAt))
            ReminderScheduler.schedule(applicationContext, task.id, nextAt)
        } else {
            app.taskRepository.save(task.copy(reminderFrequency = ReminderFrequency.NONE))
        }
        return Result.success()
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        fun workName(taskId: Long): String = "reminder_$taskId"
    }
}
