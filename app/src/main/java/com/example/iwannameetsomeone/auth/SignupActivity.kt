package com.example.iwannameetsomeone.auth


import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import com.example.iwannameetsomeone.MainActivity
import com.example.iwannameetsomeone.R
import com.example.iwannameetsomeone.utils.FirebaseRef
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_signup.*

private val TAG = "SignupActivity"

private lateinit var auth: FirebaseAuth

private var uid = ""
private var city = ""
private var age = ""
private var nickname = ""

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
            val gender = if (rbAccountMale.isChecked) "남자" else "여자"
            city = findViewById<EditText>(R.id.cityEdt).text.toString()
            age = findViewById<EditText>(R.id.ageEdt).text.toString()
            nickname = findViewById<EditText>(R.id.nickEdt).text.toString()


            auth.createUserWithEmailAndPassword(email.text.toString(), pwd.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        uid = user?.uid.toString()
                        val userModel = UserDataModel(uid, nickname, age, gender, city)

                        //데이터베이스에 저장하기
                        FirebaseRef.userInfoRef.child(uid).setValue(userModel)
//                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)

                    }
                }
        }
    }
}