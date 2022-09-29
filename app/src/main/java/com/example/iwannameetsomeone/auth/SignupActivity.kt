package com.example.iwannameetsomeone.auth


import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import com.example.iwannameetsomeone.MainActivity
import com.example.iwannameetsomeone.R
import com.example.iwannameetsomeone.utils.FirebaseRef
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_signup.*
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*

private val TAG = "SignupActivity"

private lateinit var auth: FirebaseAuth

private var uid = ""
private var location = ""
private var birth = Date()
private var nickname = ""
private var y = 0
private var m = 0
private var d = 0

class SignupActivity : AppCompatActivity() {

    var isPwDupOk = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        // Initialize Firebase Auth
        auth = Firebase.auth


        val getAction = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri ->
                profileImg.setImageURI(uri)
            }
        )
        profileImgBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            getAction.launch("image/*")
            //기존 버튼 배경 사라지게
            profileImgBtn.alpha =0f

            startActivityForResult(intent, 0)
        }

        signupSaveBtn.setOnClickListener {

            val email = findViewById<EditText>(R.id.emailEdt)
            val pwd = findViewById<EditText>(R.id.passwordEdt)
            val gender = if (rbAccountMale.isChecked) "남자" else "여자"
            val birth = findViewById<TextView>(R.id.birthTxt).text.toString()
            location = findViewById<EditText>(R.id.locationEdt).text.toString()
            nickname = findViewById<EditText>(R.id.nickEdt).text.toString()


            if (email.text?.isEmpty()!!) {
                Toast.makeText(this, "email을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (!isPwDupOk) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (nickname.isEmpty()!!) {
                Toast.makeText(this, "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show()
//            } else if (birth.isEmpty()!!) {
//                Toast.makeText(this, "나이를 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (location.isEmpty()!!) {
                Toast.makeText(this, "지역을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {

                auth.createUserWithEmailAndPassword(email.text.toString(), pwd.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            val user = auth.currentUser
                            uid = user?.uid.toString()

                            val userModel = UserDataModel(
                                uid,
                                nickname,
                                birth,
                                gender,
                                location
                            )

                            //데이터베이스에 저장하기
                            FirebaseRef.userInfoRef.child(uid).setValue(userModel)
                            uploadImage(uid)
                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)

                        }
                    }

            }
        }

        passwordEdt.addTextChangedListener {
            isPwDupOk = (it.toString() == pwDupCheckEdt.text.toString())
        }
        pwDupCheckEdt.addTextChangedListener {
            isPwDupOk = (it.toString() == passwordEdt.text.toString())
        }
    }

    private fun uploadImage(uid: String) {

        val storage = Firebase.storage
        val storageRef = storage.reference.child(uid + ".png")


        // Get the data from an ImageView as bytes
        profileImg.isDrawingCacheEnabled = true
        profileImg.buildDrawingCache()
        val bitmap = (profileImg.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = storageRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }


    }
    fun clickBirth(view: View?) {
        val birthDate = birthTxt!!.text.toString()
        val birthDates = birthDate.split("\\.").toTypedArray()
        var userYear: Int
        var userMonth: Int
        var userDate: Int
        try {
            userYear = birthDates[0].toInt()
            userMonth = birthDates[1].toInt()
            userDate = birthDates[2].toInt()
        } catch (e: Exception) {
            userYear = 1990
            userMonth = 1
            userDate = 1
        }
        val datePickerDialog = DatePickerDialog(
            this,
            android.R.style.Theme_Holo_Light_Dialog_MinWidth,
            { view, year, month, dayOfMonth ->
                y = year
                m = month + 1
                d = dayOfMonth
                birthTxt!!.text = "$y.$m.$d"
            },
            userYear,
            userMonth - 1,
            userDate
        )
        datePickerDialog.datePicker.calendarViewShown = false
        datePickerDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        datePickerDialog.show()
    }

}