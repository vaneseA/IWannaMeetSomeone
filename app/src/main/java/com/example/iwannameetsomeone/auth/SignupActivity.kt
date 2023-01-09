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
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import com.example.iwannameetsomeone.MainActivity
import com.example.iwannameetsomeone.R
import com.example.iwannameetsomeone.utils.FirebaseRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_signup.*
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*

private val TAG = "SignupActivity"

private lateinit var auth: FirebaseAuth

private var uid = ""
private var location = ""
private var nickname = ""
private var job = ""
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
            profileImgBtn.alpha = 0f

            startActivityForResult(intent, 0)
        }

//      지역 스피너 선언
        val locationSpinner = findViewById<Spinner>(R.id.locationSpinner)
        locationSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.itemList,
            android.R.layout.simple_spinner_item
        )
        locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {

                //아이템이 클릭 되면 맨 위부터 position 0번부터 순서대로 동작하게 됩니다.
                when (position) {
                    1 -> location = "서울특별시"
                    2 -> location = "경기도"
                    3 -> location = "충청도"
                    4 -> location = "강원도"
                    5 -> location = "전라도"
                    6 -> location = "경상도"
                    7 -> location = "제주"
                    else -> location = "지역 선택 안함"
                }
            }

            //          아무것도 선택되지 않은 상태
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
//      직업 스피너 선언
        val jobSpinner = findViewById<Spinner>(R.id.jobSpinner)
        jobSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.jobItemList,
            android.R.layout.simple_spinner_item
        )
        jobSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {

//              아이템이 클릭 되면 맨 위부터 position 0번부터 순서대로 동작
                when (position) {
                    1 -> job = "회사원"
                    2 -> job = "공무원/공기업"
                    3 -> job = "사업가"
                    4 -> job = "서비스직"
                    5 -> job = "전문직"
                    6 -> job = "학생"
                    else -> job = "무직"
                }
            }

            //          아무것도 선택되지 않은 상태
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }


        signupSaveBtn.setOnClickListener {

            val email = findViewById<EditText>(R.id.emailEdt)
            val pwd = findViewById<EditText>(R.id.passwordEdt)
            val gender = if (rbAccountMale.isChecked) "남자" else "여자"
            val birth = findViewById<TextView>(R.id.birthTxt).text.toString()
            val age = findViewById<TextView>(R.id.ageTxt).text.toString()
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

                            // Token
                            FirebaseMessaging.getInstance().token.addOnCompleteListener(
                                OnCompleteListener { task ->
                                    if (!task.isSuccessful) {
                                        Log.w(
                                            TAG,
                                            "Fetching FCM registration token failed",
                                            task.exception
                                        )
                                        return@OnCompleteListener
                                    }

                                    // Get new FCM registration token
                                    val token = task.result

                                    val userModel = UserDataModel(
                                        uid,
                                        nickname,
                                        birth,
                                        age,
                                        gender,
                                        location,
                                        job,
                                        token
                                    )

                                    //데이터베이스에 저장하기
                                    FirebaseRef.userInfoRef.child(uid).setValue(userModel)
                                    uploadImage(uid)
                                    startActivity(Intent(this, MainActivity::class.java))
                                })
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


//              나이로직
                val birthDay: Calendar = Calendar.getInstance()
                birthDay.set(y, m, d)

                val today: Calendar = Calendar.getInstance()
                var age: Int = today.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR)

                if (today.get(Calendar.DAY_OF_YEAR) < birthDay.get(Calendar.DAY_OF_YEAR))
                    age--

                ageTxt!!.text = age.toString() + "세"
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
