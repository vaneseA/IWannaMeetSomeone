package com.example.iwannameetsomeone.settings

import android.app.DatePickerDialog
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
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.iwannameetsomeone.R
import com.example.iwannameetsomeone.auth.LoginActivity
import com.example.iwannameetsomeone.auth.UserDataModel
import com.example.iwannameetsomeone.databinding.ActivityMyPageBinding

import com.example.iwannameetsomeone.utils.FirebaseAuthUtils
import com.example.iwannameetsomeone.utils.FirebaseRef
import com.example.iwannameetsomeone.viewpager.ViewPagerAdapter
import com.example.iwannameetsomeone.viewpager.ViewPagerFragmentStateAdapter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_my_page.*
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*

private val uid = FirebaseAuthUtils.getUid()

private val TAG = "MyPageActivity"
private val likeUserListUid = mutableListOf<String>()
private val likeUserList = mutableListOf<UserDataModel>()

private var y = 0
private var m = 0
private var d = 0


private var myLocation = ""
private var myJob = ""


lateinit var getterUid: String
lateinit var getterToken: String

class MyPageActivity : AppCompatActivity() {

    private val tabTitleArray = arrayOf(
        "내가 찜한 회원",
        "나를 찜한 회원",
        "쪽지함"
    )

private var  vBinding : ActivityMyPageBinding? = null
    // 매번 null 확인 귀찮음 -> 바인딩 변수 재선언
    private val binding get() = vBinding!!
    lateinit var mAdapter: ViewPagerFragmentStateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vBinding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout
        viewPager.adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitleArray[position]
        }.attach()


//      지역 스피너 선언
        val myLocationSpinner = findViewById<Spinner>(R.id.myLocationSpinner)

        myLocationSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.itemList,
            android.R.layout.simple_spinner_item
        )

//      직업 스피너 선언
        val myJobSpinner = findViewById<Spinner>(R.id.myJobSpinner)
        myJobSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.jobItemList,
            android.R.layout.simple_spinner_item
        )


        getMyData()


        backToTheMain.setOnClickListener {
            finish()}


        LogoutBtn.setOnClickListener {

            val auth = Firebase.auth
            auth.signOut()

            // '새 작업(task) 시작' 또는 '시작하려는 액티비티보다 상위에 존재하는 액티비티 삭제'
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

            onDestroy()
            startActivity(Intent(this, LoginActivity::class.java))


        }
        val getAction = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri ->
                myPageImg.setImageURI(uri)
            }
        )
        imgChangeBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            getAction.launch("image/*")

            startActivityForResult(intent, 0)
        }

        // 저기 내가 좋아요한 유저를 클릭하면은(Long Click)
        // 만약에 서로 좋아요한 사람이 아니면은, 메세지 못 보내도록 함
        // 메세지 보내기 창이 떠서 메세지를 보낼 수 있게 하고
        // 메세지 보내고 상대방에서 PUSH 알람 띄워주고
        updateBtn.setOnClickListener {
            val genderCheck = if (rbProfileSet_Male.isChecked) "남자" else "여자"

            updateUserData(
                uid,
                myNickname.text.toString(),
                myBirth.text.toString(),
                myAge.text.toString(),
                genderCheck,
                myLocation,
                myJob
            )
            uploadImageForUpdate(uid)
            finish()
        }

    }

    private fun getMyData() {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(UserDataModel::class.java)


                myNickname.setText(data!!.nickname)
                myBirth.setText(data!!.birth)
                myAge.setText(data!!.age)


//              스피너 지역 설정
                when (data!!.location.toString()) {
                    "서울특별시" -> myLocationSpinner.setSelection(1)
                    "경기도" -> myLocationSpinner.setSelection(2)
                    "충청도" -> myLocationSpinner.setSelection(3)
                    "강원도" -> myLocationSpinner.setSelection(4)
                    "전라도" -> myLocationSpinner.setSelection(5)
                    "경상도" -> myLocationSpinner.setSelection(6)
                    "제주" -> myLocationSpinner.setSelection(7)
                    else -> myLocationSpinner.setSelection(0)
                }

//              스피너 직업 설정
                when (data!!.job.toString()) {
                    "회사원" -> myJobSpinner.setSelection(1)
                    "공무원/공기업" -> myJobSpinner.setSelection(2)
                    "사업가" -> myJobSpinner.setSelection(3)
                    "서비스직" -> myJobSpinner.setSelection(4)
                    "전문직" -> myJobSpinner.setSelection(5)
                    "학생" -> myJobSpinner.setSelection(6)
                    "무직" -> myJobSpinner.setSelection(7)
                    else -> null
                }

                myLocationSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View,
                            position: Int,
                            id: Long
                        ) {

                            //아이템이 클릭 되면 맨 위부터 position 0번부터 순서대로 동작하게 됩니다.
                            when (position) {
                                1 -> myLocation = "서울특별시"
                                2 -> myLocation = "경기도"
                                3 -> myLocation = "충청도"
                                4 -> myLocation = "강원도"
                                5 -> myLocation = "전라도"
                                6 -> myLocation = "경상도"
                                7 -> myLocation = "제주"
                                else -> myLocation = "지역 선택 안함"
                            }
                        }

                        //          아무것도 선택되지 않은 상태
                        override fun onNothingSelected(parent: AdapterView<*>) {

                        }
                    }
                myJobSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View,
                        position: Int,
                        id: Long
                    ) {

                        //아이템이 클릭 되면 맨 위부터 position 0번부터 순서대로 동작
                        when (position) {
                            1 -> myJob = "회사원"
                            2 -> myJob = "공무원/공기업"
                            3 -> myJob = "사업가"
                            4 -> myJob = "서비스직"
                            5 -> myJob = "전문직"
                            6 -> myJob = "학생"
                            else -> myJob = "무직"
                        }
                    }

                    //          아무것도 선택되지 않은 상태
                    override fun onNothingSelected(parent: AdapterView<*>) {
                        data!!.job
                    }
                }
                //라디오버튼 성별 저장
                if (data.gender == "여자") {
                    rbProfileSet_Female.setChecked(true)
                } else {
                    rbProfileSet_Male.setChecked(true)
                }
                val storageRef = Firebase.storage.reference.child(data.uid + ".png")
                storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->

                    if (task.isSuccessful) {
                        Glide.with(baseContext)
                            .load(task.result)
                            .into(myPageImg)

                    }

                })

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }


    private fun updateUserData(
        uid: String,
        nickname: String,
        birth: String,
        age: String,
        gender: String,
        location: String,
        job: String
    ) {
        val genderCheck = if (rbProfileSet_Male.isChecked) "남자" else "여자"
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        val userInfo =
            UserDataModel(
                dbRef.key.toString(),
                myNickname.text.toString(),
                myBirth.text.toString(),
                myAge.text.toString(),
                genderCheck,
                myLocation,
                myJob
            )
        dbRef.setValue(userInfo)
            .addOnSuccessListener {
                Toast.makeText(this, "수정완료", Toast.LENGTH_SHORT).show()
            }

    }

    fun clickBirth(view: View?) {
        val birthDate = myBirth!!.text.toString()
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
                myBirth!!.text = "$y.$m.$d"
                //나이로직
                val birthDay: Calendar = Calendar.getInstance()
                birthDay.set(y, m, d)

                val today: Calendar = Calendar.getInstance()
                var age: Int = today.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR)

                if (today.get(Calendar.DAY_OF_YEAR) < birthDay.get(Calendar.DAY_OF_YEAR))
                    age--

                myAge!!.text = age.toString() + "세"
            },
            userYear,
            userMonth - 1,
            userDate
        )
        datePickerDialog.datePicker.calendarViewShown = false
        datePickerDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        datePickerDialog.show()
    }

    private fun uploadImageForUpdate(uid: String) {

        val storage = Firebase.storage
        val storageRef = storage.reference.child(uid + ".png")


        // Get the data from an ImageView as bytes
        myPageImg.isDrawingCacheEnabled = true
        myPageImg.buildDrawingCache()
        val bitmap = (myPageImg.drawable as BitmapDrawable).bitmap
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
    override fun onDestroy() {
        vBinding = null
        super.onDestroy()
    }



}