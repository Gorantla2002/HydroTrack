package uk.ac.tees.mad.s3548263.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import uk.ac.tees.mad.s3548263.MainActivity
import uk.ac.tees.mad.s3548263.R
import java.util.Calendar
import java.util.concurrent.TimeUnit


class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        if (!isWithinReminderWindow()) {
            return Result.success()
        }

        showNotification()
        return Result.success()
    }


    private fun isWithinReminderWindow(): Boolean {
        val sharedPrefs = context.getSharedPreferences("hydro_prefs", Context.MODE_PRIVATE)
        val startTime = sharedPrefs.getString("reminder_start_time", "08:00") ?: "08:00"
        val endTime = sharedPrefs.getString("reminder_end_time", "22:00") ?: "22:00"

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentTimeMinutes = currentHour * 60 + currentMinute

        val startParts = startTime.split(":")
        val startTimeMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()

        val endParts = endTime.split(":")
        val endTimeMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()

        return currentTimeMinutes in startTimeMinutes..endTimeMinutes
    }


    private fun showNotification() {
        val channelId = "hydration_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to drink water and track nutrition"
                enableVibration(true)
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val messages = listOf(
            "Time to hydrate! 💧",
            "Don't forget to drink water! 💦",
            "Stay hydrated, stay healthy! 🌊",
            "Your body needs water! 💙",
            "Hydration reminder! 💧",
            "Time for a water break! 🥤",
            "Keep your body hydrated! 💧"
        )

        val randomMessage = messages.random()

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(randomMessage)
            .setContentText("Track your water and nutrition intake now!")
            .setSmallIcon(R.drawable.ic_water_drop)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        private const val WORK_NAME = "hydration_reminder_work"
        private const val TAG = "NotificationWorker"


        fun scheduleReminders(context: Context, intervalMinutes: Long) {
            val actualInterval = intervalMinutes.coerceAtLeast(15)

            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
                actualInterval,
                TimeUnit.MINUTES,
                15,
                TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }

        fun cancelReminders(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            WorkManager.getInstance(context).cancelAllWorkByTag(TAG)
        }


        fun areRemindersScheduled(context: Context): Boolean {
            val workInfos = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(WORK_NAME)
                .get()

            return workInfos.any { workInfo ->
                workInfo.state == WorkInfo.State.ENQUEUED ||
                        workInfo.state == WorkInfo.State.RUNNING
            }
        }

        fun saveReminderWindow(context: Context, startTime: String, endTime: String) {
            val sharedPrefs = context.getSharedPreferences("hydro_prefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().apply {
                putString("reminder_start_time", startTime)
                putString("reminder_end_time", endTime)
                apply()
            }
        }

        fun getReminderWindow(context: Context): Pair<String, String> {
            val sharedPrefs = context.getSharedPreferences("hydro_prefs", Context.MODE_PRIVATE)
            val startTime = sharedPrefs.getString("reminder_start_time", "08:00") ?: "08:00"
            val endTime = sharedPrefs.getString("reminder_end_time", "22:00") ?: "22:00"
            return Pair(startTime, endTime)
        }
    }
}