package com.example.iwannameetsomeone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.iwannameetsomeone.auth.LoginActivity
import com.example.iwannameetsomeone.settings.MyPageActivity
import com.example.iwannameetsomeone.utils.FirebaseAuthUtils

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        val uid = FirebaseAuthUtils.getUid()

        if (uid == "null") {//로그인이 안되어있으면 LoginActivity로

            Handler().postDelayed({
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
            }, 3000)
        } else {//로그인이 돼있으면 MyPageActivity로
            Handler().postDelayed({
                val intent = Intent(this, MyPageActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
            }, 3000)
        }
    }
}