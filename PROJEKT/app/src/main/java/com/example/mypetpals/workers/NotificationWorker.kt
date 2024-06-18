package com.example.mypetpals.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.mypetpals.R

class NotificationWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        val petName = inputData.getString("petName")
        val description = inputData.getString("description")

        createNotificationChannel()

        val notification = NotificationCompat.Builder(applicationContext, "pet_notifications")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Notification for $petName")
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()



        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pet Notifications"
            val descriptionText = "Notifications for your pets"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("pet_notifications", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}