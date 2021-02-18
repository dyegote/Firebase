package com.dyegote.firebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.sleep(2000);
        setTheme(R.style.Theme_Firebase);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.notificationFCM()
        this.firebaseRemoteConfig();


    }
    val TAG_FCM = "FCM"

    //Enviar mensajes de pruba a traves del Compositor de Notificaciones
    //https://console.firebase.google.com/u/0/project/test-dyegote/notification?hl=es-419
    private fun notificationFCM(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG_FCM, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            Log.d(TAG_FCM, "Token del dispositivo: ${token.toString()}")
            Toast.makeText(baseContext, token, Toast.LENGTH_LONG).show()
        })

        //Temas (Topics)
        //Para enviar notificaciones a un grupo de usuarios
        FirebaseMessaging.getInstance().subscribeToTopic("Autolight")
                .addOnCompleteListener { task ->
                    var msg = "Subscripcion correcta"
                    if (!task.isSuccessful) {
                        msg = "Error de subscripcion"
                    }
                    Log.d(TAG_FCM, msg)
                    Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
                }

    }

    private fun firebaseRemoteConfig(){

        val configSettings :  FirebaseRemoteConfigSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        val firebaseConfig : FirebaseRemoteConfig = Firebase.remoteConfig
        firebaseConfig.setConfigSettingsAsync(configSettings)
        firebaseConfig.setDefaultsAsync(mapOf("nombre_app" to "desconocido"))//valores por defecto

        //Recuperar datos de cnfiguracion
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener{
            if(it.isSuccessful)
                showAlert(Firebase.remoteConfig.getString("nombre_app"));
        }
    }

    private fun showAlert(message : String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Remote Config")
        builder.setMessage(message )
        builder.setPositiveButton("ACEPTAR") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

}