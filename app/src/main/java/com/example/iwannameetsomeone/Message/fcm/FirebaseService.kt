package com.example.iwannameetsomeone.Message.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.iwannameetsomeone.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


// 하고 싶은 것은, 유저의 토큰 정보를 받아와서 -> ok
// Firebase 서버로 메세지 보내라고 명령 하고 -> ok
// Firebase 서버에서 앱으로 메세지 보내주고 -> ok
// 앱에서는 메세지를 받아서 -> ok
// 앱에서는 알람을 띄워줌 -> ok


class FirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["title"].toString()
        val body = message.data["content"].toString()


        // 알림 채널 시스템에 등록
        createNotificationChannel()

        // 알림 보내기
        sendNotification(title, body)

    }


    private fun createNotificationChannel() {
        // API 26 이상에서만 NotificationChannel을 생성(API 25 이하는 지원하지 않음)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Test_Channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(title: String, body: String) {
        var builder = NotificationCompat.Builder(this, "Test_Channel")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {
            notify(123, builder.build())
        }
    }

}
