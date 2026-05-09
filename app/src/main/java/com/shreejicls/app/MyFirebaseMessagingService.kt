package com.shreejicls.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        getSharedPreferences("session", Context.MODE_PRIVATE).edit().putString("fcm_token", token).apply()
        sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        android.util.Log.d("SHREEJI_FCM", "Message Received: ${message.data}")
        val title = message.notification?.title ?: message.data["title"] ?: "ShreeJi Classes"
        val body = message.notification?.body ?: message.data["body"] ?: "You have a new update"
        showNotification(title, body)
    }

    private fun showNotification(title: String, message: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "shreeji_fcm"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Important Updates", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Urgent notifications for fees and notes"
            channel.enableLights(true)
            channel.enableVibration(true)
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        manager.notify(Random.nextInt(), notif)
    }

    private fun sendTokenToServer(token: String) {
        val cookies = getSharedPreferences("session", Context.MODE_PRIVATE).getString("cookies", null) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://shreeji-classes.onrender.com/api/fcm-token")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Cookie", cookies)
                conn.doOutput = true
                val body = JSONObject().put("token", token).toString()
                OutputStreamWriter(conn.outputStream).use { it.write(body) }
                conn.responseCode
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
