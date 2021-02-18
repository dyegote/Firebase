package com.dyegote.firebase

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class TestCloudMessagingService : FirebaseMessagingService() {

    //Para recibir y gestionar notificacion cuando a app NO esta en segundo plano
    override fun onMessageReceived(message: RemoteMessage) {
        Looper.prepare();
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(baseContext,message.notification?.title, Toast.LENGTH_LONG).show();
        }

        Looper.loop();
    }
}