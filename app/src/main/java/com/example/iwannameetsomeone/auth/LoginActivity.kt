package com.example.iwannameetsomeone.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.iwannameetsomeone.R
import kotlinx.android.synthetic.main.activity_intro.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        signupBtn.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))

        }
        loginBtn.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}