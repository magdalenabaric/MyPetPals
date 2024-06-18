package com.example.mypetpals


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.os.Build
import androidx.core.content.ContextCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val petName = intent.getStringExtra("petName")
        val description = intent.getStringExtra("description")

        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, "pet_notifications")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Notification for $petName")
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(context)) {
                notify((System.currentTimeMillis() % 10000).toInt(), notification)
            }
        } else {
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pet Notifications"
            val descriptionText = "Notifications for your pets"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("pet_notifications", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}