package com.example.myapplication.notifications

import android.Manifest
import android.content.*
import android.app.*
import android.content.pm.PackageManager
import com.example.myapplication.MainActivity
import androidx.core.app.NotificationCompat
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R

object Notifications {
    const val CHANNEL_ID = "sensor_channel"

    fun createNotificationChannel(context: Context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Sensor notification"
            val descriptionText = "Notification from the sensor trigger"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply{
                description = descriptionText
                enableVibration(true) //Tried to get a pop-up
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun triggerNotification(context: Context, acceleration: Double){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { //over Android 13+
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return //Notfication not triggered if there is no permission granted
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        val fullScreenIntent = Intent(context, MainActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 0, fullScreenIntent, PendingIntent.FLAG_IMMUTABLE
        )

        //Tried to add a lot of properties for the notification to get a pop-up
        //Didn't work
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.catstare) //Doesn't show properly
            .setContentTitle("!ATTENTION!")
            .setContentText("Wow you are going fast: ${String.format("%.2f", acceleration)}") //Display acceleration in notification real-time
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)


        with(NotificationManagerCompat.from(context)){
            notify(1, builder.build())
        }
    }


}