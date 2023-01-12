package com.example.iwannameetsomeone

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.iwannameetsomeone.auth.UserDataModel
import com.example.iwannameetsomeone.databinding.ActivityMainBinding
import com.example.iwannameetsomeone.settings.MyPageActivity
import com.example.iwannameetsomeone.slider.CardStackAdapter
import com.example.iwannameetsomeone.utils.FirebaseAuthUtils
import com.example.iwannameetsomeone.utils.FirebaseRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    // 태그
    private val TAG = "MainActivity"


    // (전역변수) 바인딩 객체 선언
    private var vBinding: ActivityMainBinding? = null

    // 매번 null 확인 귀찮음 -> 바인딩 변수 재선언
    private val binding get() = vBinding!!

    // 카드스택뷰
    lateinit var cardStackAdapter: CardStackAdapter
    private lateinit var manager: CardStackLayoutManager

    // 사용자 데이터 리스트
    private val usersDataList = mutableListOf<UserDataModel>()

    // 사용자 수 세기
    private var userCount = 0

    // 현재 사용자의 성별
    private lateinit var curruntUserGender: String


    // UID
    private var uid = FirebaseAuthUtils.getUid()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 자동 생성된 뷰바인딩 클래스에서의 inflate 메서드 활용
        // -> 액티비티에서 사용할 바인딩 클래스의 인스턴스 생성
        vBinding = ActivityMainBinding.inflate(layoutInflater)

        // -> 생성된 뷰를 액티비티에 표시
        setContentView(binding.root)

        myInfo.setOnClickListener {

            startActivity(Intent(this, MyPageActivity::class.java))
        }


        // 카드스택뷰
        manager = CardStackLayoutManager(baseContext, object : CardStackListener {

            override fun onCardSwiped(direction: Direction?) {

                // 왼쪽(관심없음)
                if (direction == Direction.Left) {

                }
                // 오른쪽(좋아요)
                if (direction == Direction.Right) {
                    // 해당 카드(사용자) 좋아요 처리
                    myLikeUser(uid, usersDataList[userCount].uid.toString())
                }

                // 넘긴 프로필의 수를 셈
                userCount += 1


                // 프로필 전부 다 봤을 때
                if (userCount == usersDataList.count()) {

                    // 자동으로 새로고침
                    getUserDataList(curruntUserGender)
                    Toast.makeText(this@MainActivity, "모든 프로필을 확인했습니다", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCardDragging(direction: Direction?, ratio: Float) {}
            override fun onCardRewound() {}
            override fun onCardCanceled() {}
            override fun onCardAppeared(view: View?, position: Int) {}
            override fun onCardDisappeared(view: View?, position: Int) {}

        })

        // 카드스택어댑터에 데이터 넘김
        cardStackAdapter = CardStackAdapter(baseContext, usersDataList)
        cardStackView.layoutManager = manager
        cardStackView.adapter = cardStackAdapter

        // 현재 사용자 정보
        getMyUserData()
    }


    private fun getMyUserData() {

        // 데이터베이스에서 컨텐츠의 세부정보를 검색
        val postListener = object : ValueEventListener {

            // 데이터스냅샷 내 사용자 데이터 출력
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                // 프사 제외한 나머지 정보
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                // 현재 사용자의 성별
                curruntUserGender = data?.gender.toString()


                // 현재 사용자와 성별이 반대인 사용자 목록
                getUserDataList(curruntUserGender)

            }

            // 실패시
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // 파이어베이스 내 데이터의 변화(추가)를 알려줌
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }

    // 전체 사용자 정보
    private fun getUserDataList(curruntUserGender: String) {

        // 데이터베이스에서 컨텐츠의 세부정보를 검색
        val postListener = object : ValueEventListener {

            // 데이터스냅샷 내 사용자 데이터 출력
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                // 데이터스냅샷 내 사용자 데이터 출력
                for (dataModel in dataSnapshot.children) {

                    // 다른 사용자들 정보 가져옴
                    val user = dataModel.getValue(UserDataModel::class.java)

                    // 현재 사용자와 다른 성별인 사용자만 불러옴
                    if (user!!.gender.toString().equals(curruntUserGender)) {
                    } else {
                        usersDataList.add(user)
                    }
                }
                // 동기화(새로고침) -> 리스트 크기 및 아이템 변화를 어댑터에 알림
                cardStackAdapter.notifyDataSetChanged()
            }

            //실패시
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // 파이어베이스 내 데이터의 변화(추가)를 알려줌
        FirebaseRef.userInfoRef.addValueEventListener(postListener)

    }


    //유저의 좋아요를 표시하는 부분
    //필요한 데이터값 양쪽의 uid값
    private fun myLikeUser(myUid: String, otherUid: String) {

        // (카드 오른쪽으로 넘기면) 좋아요 값 true로 설정
        FirebaseRef.myLikeRef.child(myUid).child(otherUid).setValue("true")
        // 나를 좋아하는 사람을 true로 설정
        FirebaseRef.likeMeRef.child(otherUid).child(myUid).setValue("true")

        // 좋아요 목록
        getOutherUserLikeList(otherUid)
        // DB
        // └─userLike
        //   └─현재 사용자의 UID
        //     └─현재 사용자가 좋아요 한 사용자의 UID : "true"
    }


    private fun getOutherUserLikeList(otherUid: String) {

        // 데이터베이스에서 컨텐츠의 세부정보를 검색
        val postListener = object : ValueEventListener {

            // 데이터스냅샷 내 사용자 데이터 출력
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //여기 리스트 안에서 나의 uid가 있는지 확인
                //내가 좋아요를 누른 사람의 좋아요 리스트를 불러온다
                //상대 좋아요 리스트에서 내 uid가 있는지 확인
                for (dataModel in dataSnapshot.children) {

                    // 다른 사용자가 좋아요 한 사용자 목록에
                    val likeUserKey = dataModel.key.toString()

                    // 현재 사용자가 포함돼 있으면
                    if (likeUserKey.equals(uid)) {
                        // 알림 채널 시스템에 등록
                        createNotificationChannel()
                        // 알림 보내기
                        sendNotification()
                    }

                }
            }

            // 실패시
            override fun onCancelled(databaseError: DatabaseError) {

                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.myLikeRef.child(otherUid).addValueEventListener(postListener)
    }

    // 알림 채널 시스템에 등록
    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val name = "name"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Test_Channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    // 푸시 알림(매칭)
    private fun sendNotification() {
        var builder = NotificationCompat.Builder(this, "Test_Channel")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("매칭완료")
            .setContentText("매칭이 완료되었습니다.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {
            notify(123, builder.build())
        }
    }

    // 액티비티 파괴시
    override fun onDestroy() {

        // 바인딩 클래스 인스턴스 참조를 정리 -> 메모리 효율이 좋아짐
        vBinding = null
        super.onDestroy()

    }


}

