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
import com.example.iwannameetsomeone.databinding.ActivitySignupBinding
import com.example.iwannameetsomeone.utils.FirebaseRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*

// (전역변수) 바인딩 객체 선언
private var vBinding: ActivitySignupBinding? = null

// 매번 null 확인 귀찮음 -> 바인딩 변수 재선언
private val binding get() = vBinding!!

//AUTH
private lateinit var auth: FirebaseAuth

//UID
private var uid = ""
private var location = ""
private var nickname = ""
private var job = ""
private var y = 0
private var m = 0
private var d = 0

class SignupActivity : AppCompatActivity() {

    //패스워드 중복 체크
    var isPwDupOk = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 자동 생성된 뷰바인딩 클래스에서의 inflate 메서드 활용
        // -> 액티비티에서 사용할 바인딩 클래스의 인스턴스 생성
        vBinding = ActivitySignupBinding.inflate(layoutInflater)
        // -> 생성된 뷰를 액티비티에 표시
        setContentView(binding.root)

        auth = Firebase.auth


        val getAction = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri ->
                binding.profileImg.setImageURI(uri)
            }
        )
        //사진추가 버튼
        binding.profileImgBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            getAction.launch("image/*")
            //기존 버튼 배경 사라지게
            binding.profileImgBtn.alpha = 0f

            startActivityForResult(intent, 0)
        }

        //지역 스피너 선언
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

                //아이템이 클릭 되면 맨 위부터 position 0번부터 순서대로 동작
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

            //아무것도 선택되지 않은 상태
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        //직업 스피너 선언
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

            //아무것도 선택되지 않은 상태
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        //회원가입 완료버튼
        binding.signupSaveBtn.setOnClickListener {

            val email = findViewById<EditText>(R.id.emailEdt)
            val pwd = findViewById<EditText>(R.id.passwordEdt)
            val gender = if (binding.rbAccountMale.isChecked) "남자" else "여자"
            val birth = findViewById<TextView>(R.id.birthTxt).text.toString()
            val age = findViewById<TextView>(R.id.ageTxt).text.toString()
            nickname = findViewById<EditText>(R.id.nickEdt).text.toString()

            if (email.text?.isEmpty()!!) {//아이디 공란일 때
                Toast.makeText(this, "email을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (!isPwDupOk) {//패스워드 일치하지 않을 때
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (nickname.isEmpty()!!) {//닉네임 공란일 때
                Toast.makeText(this, "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email.text.toString(), pwd.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            uid = user?.uid.toString()

                            // 토큰
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
        //비밀번호 입력 변화처리
        binding.passwordEdt.addTextChangedListener {
            isPwDupOk = (it.toString() == binding.pwDupCheckEdt.text.toString())
        }
        binding.pwDupCheckEdt.addTextChangedListener {
            isPwDupOk = (it.toString() == binding.passwordEdt.text.toString())
        }
    }

    //이미지 업로드
    private fun uploadImage(uid: String) {

        val storage = Firebase.storage
        val storageRef = storage.reference.child(uid + ".png")


        // Get the data from an ImageView as bytes
        binding.profileImg.isDrawingCacheEnabled = true
        binding.profileImg.buildDrawingCache()
        val bitmap = (binding.profileImg.drawable as BitmapDrawable).bitmap
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

    //생년월일 선택 스피너
    fun clickBirth(view: View?) {
        val birthDate = binding.birthTxt!!.text.toString()
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
                binding.birthTxt!!.text = "$y.$m.$d"


                //나이로직
                val birthDay: Calendar = Calendar.getInstance()
                birthDay.set(y, m, d)
                val today: Calendar = Calendar.getInstance()
                var age: Int = today.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR)
                if (today.get(Calendar.DAY_OF_YEAR) < birthDay.get(Calendar.DAY_OF_YEAR))
                    age--
                binding.ageTxt!!.text = age.toString() + "세"
            },
            userYear,
            userMonth - 1,
            userDate
        )


        datePickerDialog.datePicker.calendarViewShown = false
        datePickerDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        datePickerDialog.show()


    }

    // 액티비티 파괴시
    override fun onDestroy() {

        // 바인딩 클래스 인스턴스 참조를 정리 -> 메모리 효율이 좋아짐
        vBinding = null
        super.onDestroy()

    }

}
