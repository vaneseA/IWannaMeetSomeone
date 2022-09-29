package com.example.iwannameetsomeone.auth


import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.iwannameetsomeone.MainActivity
import com.example.iwannameetsomeone.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_signup.*

private lateinit var auth: FirebaseAuth
class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        // Initialize Firebase Auth
        auth = Firebase.auth

        val signupSaveBtn = findViewById<TextView>(R.id.signupSaveBtn)
        signupSaveBtn.setOnClickListener {

            val email = findViewById<EditText>(R.id.emailEdt)
            val pwd = findViewById<EditText>(R.id.passwordEdt)

            auth.createUserWithEmailAndPassword(email.text.toString(), pwd.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser
                        Log.d(TAG,user?.uid.toString())
                        startActivity(Intent(this, MainActivity::class.java))
//                    val user = auth.currentUser
//                    updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
//                    Toast.makeText(baseContext, "Authentication failed.",
//                        Toast.LENGTH_SHORT).show()
//                    updateUI(null)
                    }
                }
        }
    }
}