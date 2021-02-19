package com.dyegote.firebase

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.text.CaseMap
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dyegote.firebase.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class AuthActivity : AppCompatActivity() {

    val TAG_FCM = "FCM"
    private val GOOGLE_SIGN_INT = 100
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.sleep(2000);
        setTheme(R.style.Theme_Firebase);
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        val  view = binding.root
        setContentView(view)

        this.configView()
        this.notificationFCM()
        this.firebaseRemoteConfig()
        this.session()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_SIGN_INT){
            try {
                val task : Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                if(account != null){
                    val credential : AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(){
                        if(it.isSuccessful)
                            showHome(account.email ?:"", ProviderType.GOOGLE)
                        else
                            showAlert("Error", "No se pudo autenticar")
                    }
                }
            }
            catch (e: ApiException){
                e.message?.let { showAlert("Error", it) }
            }
        }
    }

    private fun configView(){
        title = "Authentication"

        binding.googleButton.setOnClickListener {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleCliente: GoogleSignInClient = GoogleSignIn.getClient(this, googleConf)
            googleCliente.signOut()//para cerrar sesion por si hay otra cueta
            startActivityForResult(googleCliente.signInIntent,GOOGLE_SIGN_INT)
        }
    }

    private fun session(){
        val prefs : SharedPreferences = getSharedPreferences(
            getString(R.string.key_auth),
            Context.MODE_PRIVATE
        );
        val email: String? = prefs.getString("email", null)
        val provider: String? = prefs.getString("provider", null)

        if(email != null && provider != null){
            showHome(email, ProviderType.valueOf(provider))
        }

    }

    private fun showHome(email: String, provider: ProviderType){
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("email", email)
        intent.putExtra("provider", provider.name)
        startActivity(intent)
    }

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


    }

    private fun showAlert(title: String, message: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("ACEPTAR", null)
        builder.show()
    }

}