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
import com.example.iwannameetsomeone.MainActivity
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


//Uid
private val uid = FirebaseAuthUtils.getUid()

//TAG
private val TAG = "MyPageActivity"

private var myLocation = ""
private var myJob = ""

lateinit var getterUid: String
lateinit var getterToken: String

class MyPageActivity : AppCompatActivity() {

    //탭 타이틀 목록
    private val tabTitleArray = arrayOf(
        "내가 찜한 회원",
        "나를 찜한 회원",
        "쪽지함"
    )

    // (전역변수) 바인딩 객체 선언
    private var vBinding: ActivityMyPageBinding? = null

    // 매번 null 확인 귀찮음 -> 바인딩 변수 재선언
    private val binding get() = vBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vBinding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //뷰페이저, 탭레이아웃 정의
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        //뷰페이저 어댑터 연결
        viewPager.adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)

        //탭 레이아웃 위치에 따른 타이틀 연결
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitleArray[position]
        }.attach()


        //지역 스피너 어댑터 연결
        binding.myLocationSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.itemList,
            android.R.layout.simple_spinner_item
        )

        //지역 스피너 어댑터 연결
        binding.myJobSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.jobItemList,
            android.R.layout.simple_spinner_item
        )

        //내 데이터 받아옴
        getMyData()

        //추천회원(메인화면 액티비티)으로
        binding.backToTheMain.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        //로그아웃 버튼
        binding.LogoutBtn.setOnClickListener {

            //파이어베이스 로그아웃
            val auth = Firebase.auth
            auth.signOut()

            // '새 작업(task) 시작' 또는 '시작하려는 액티비티보다 상위에 존재하는 액티비티 삭제'
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

            //onDestroy()를 설정해야 다른 아이디로 로그인할 때 MYPAGE에 새로 로그인한 사람의 정보를 받아올 수 있다.
            onDestroy()

            startActivity(Intent(this, LoginActivity::class.java))


        }
        //이미지 콜백
        val getAction = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri ->
                binding.myPageImg.setImageURI(uri)
            }
        )

        //이미지 변경 버튼
        binding.imgChangeBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            getAction.launch("image/*")

            startActivityForResult(intent, 0)
        }

        //수정완료 버튼
        binding.updateBtn.setOnClickListener {

            //라디오박스 체크에 따라 성별 String 변경
            val genderCheck = if (rbProfileSet_Male.isChecked) "남자" else "여자"

            //유저데이터 입력
            updateUserData(
                uid,
                myNickname.text.toString(),
                myBirth.text.toString(),
                myAge.text.toString(),
                genderCheck,
                myLocation,
                myJob,
            )
            //
            uploadImageForUpdate(uid)
            startActivity(Intent(this, MainActivity::class.java))
        }

    }

    //내 정보를 가져오는 함수
    private fun getMyData() {

        // 데이터베이스에서 컨텐츠의 세부정보를 검색
        val postListener = object : ValueEventListener {

            // 데이터스냅샷 내 사용자 데이터 출력
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                //UserDataModel안의 데이터
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                //UserDataModel안의 데이터를 각 항목에 입력
                binding.myNickname.setText(data!!.nickname)
                binding.myBirth.setText(data!!.birth)
                binding.myAge.setText(data!!.age)


                //스피너 지역 설정
                //데이터에 들어있는 String값에 따라 setSelection(Int)설정
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

                //스피너 직업 설정
                //데이터에 들어있는 String값에 따라 setSelection(Int)설정
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

                //스피너 position 선택
                binding.myLocationSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View,
                            position: Int,
                            id: Long
                        ) {

                            //아이템이 클릭 되면 맨 위부터 position 0번부터 순서대로 동작
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

                        //아무것도 선택되지 않은 상태
                        override fun onNothingSelected(parent: AdapterView<*>) {

                        }
                    }

                //스피너 position 선택
                binding.myJobSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
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

                        //아무것도 선택되지 않은 상태
                        override fun onNothingSelected(parent: AdapterView<*>) {
                            data!!.job
                        }
                    }

                //라디오버튼 성별 저장
                if (data.gender == "여자") {//data.gender의 String이 여자일 때 라디오박스 체크
                    rbProfileSet_Female.setChecked(true)
                } else {
                    rbProfileSet_Male.setChecked(true)
                }

                // uid.png로 파이어베이스 스토리지에 있는 데이터 불러옴
                val storageRef = Firebase.storage.reference.child(data.uid + ".png")

                //파이어베이스에서 downloadUrl를 가져옴
                storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->

                    //데이터를 성공적으로 가져올 경우
                    if (task.isSuccessful) {
                        Glide.with(baseContext)
                            .load(task.result)
                            .into(binding.myPageImg)

                    }

                })

            }

            //실패시
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // 파이어베이스 내 데이터의 변화(추가)를 알려줌
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }

    //내 정보 데이터를 업데이트하는 함수
    private fun updateUserData(
        uid: String,
        nickname: String,
        birth: String,
        age: String,
        gender: String,
        location: String,
        job: String,
    ) {
        //라디오박스 체크에 따라 성별 String 변경
        val genderCheck = if (rbProfileSet_Male.isChecked) "남자" else "여자"

        //데이터베이스에서 유저를 받아옴
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid)

        //유저정보
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

        //유저정보에 userInfo 값을 세팅
        dbRef.setValue(userInfo)
            .addOnSuccessListener {//성공시 토스트알림
                Toast.makeText(this, "수정완료", Toast.LENGTH_SHORT).show()
            }

    }

    //생년월일 스피너
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
                var y = year
                var m = month + 1
                var d = dayOfMonth
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

    //이미지업로드 함수
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
        }.addOnSuccessListener { taskSnapshot ->
        }
    }


    // 액티비티 파괴시
    override fun onDestroy() {

        // 바인딩 클래스 인스턴스 참조를 정리 -> 메모리 효율이 좋아짐
        vBinding = null
        super.onDestroy()

    }

}