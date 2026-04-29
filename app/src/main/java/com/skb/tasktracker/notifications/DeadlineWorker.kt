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
import java.util.concurrent.TimeUnit

class DeadlineWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as TaskTrackerApp
        val until = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)
        val due = app.taskRepository.upcomingDue(until)
        if (due.isEmpty()) return Result.success()
        if (!hasNotificationPermission()) return Result.success()

        val nm = NotificationManagerCompat.from(applicationContext)
        due.forEach { task ->
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pi = PendingIntent.getActivity(
                applicationContext,
                task.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(applicationContext, TaskTrackerApp.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Дедлайн скоро: ${task.title}")
                .setContentText("Срок выполнения наступает в течение 24 часов")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build()
            nm.notify(task.id.toInt(), notification)
            app.taskRepository.markNotified(task.id)
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
        const val WORK_NAME = "deadline_worker"
    }
}
