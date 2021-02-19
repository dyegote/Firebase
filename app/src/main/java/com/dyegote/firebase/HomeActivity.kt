package com.dyegote.firebase

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dyegote.firebase.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Recuperar ,mensaje intent
        val bundle : Bundle? = intent.extras
        val email : String? = bundle?.getString("email")
        val provider : String? = bundle?.getString("provider")

        //Guardar preferences
        val prefs : SharedPreferences.Editor = getSharedPreferences(getString(R.string.key_auth), Context.MODE_PRIVATE).edit();
        prefs.putString("email",email);
        prefs.putString("provider",provider)
        prefs.apply()

        if (email != null && provider != null) {
            this.configView(email,provider)
        }

        //Recuperar datos de cnfiguracion de Remote Config
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener{
            if(it.isSuccessful)
                showAlert("Remote Config", Firebase.remoteConfig.getString("nombre_app"));
        }

    }

    private fun configView(email: String, provider: String){
        binding.emailTextView.text = email
        binding.providerTextView.text = provider
        binding.googleLogout.setOnClickListener {
            val prefs : SharedPreferences.Editor = getSharedPreferences(getString(R.string.key_auth), Context.MODE_PRIVATE).edit();
            prefs.clear()
            prefs.apply()
            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }

    }

    private fun showAlert(title: String, message: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("ACEPTAR", null)
        builder.show()
    }
}