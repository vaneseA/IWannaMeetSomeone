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
import com.example.iwannameetsomeone.settings.MyPageActivity
import com.example.iwannameetsomeone.slider.CardStackAdapter
import com.example.iwannameetsomeone.utils.FirebaseAuthUtils
import com.example.iwannameetsomeone.utils.FirebaseRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val likeUserList = mutableListOf<UserDataModel>()

    lateinit var cardStackAdapter: CardStackAdapter
    lateinit var manager: CardStackLayoutManager

    private val TAG = "MainActivity"

    private val usersDataList = mutableListOf<UserDataModel>()

    private var userCount = 0

    private var uid = FirebaseAuthUtils.getUid()

    private lateinit var curruntUserGender: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        myInfo.setOnClickListener {

            startActivity(Intent(this, MyPageActivity::class.java))
        }


        val cardStackView = findViewById<CardStackView>(R.id.cardStackView)

        manager = CardStackLayoutManager(baseContext, object : CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {

            }

            override fun onCardSwiped(direction: Direction?) {

                if (direction == Direction.Right) {
                    userLikeOtherUser(uid, usersDataList[userCount].uid.toString())
                }
                if (direction == Direction.Left) {

                }

                userCount = userCount + 1


                if (userCount == usersDataList.count()) {
                    getUserDataList(curruntUserGender)
                }
            }

            override fun onCardRewound() {

            }

            override fun onCardCanceled() {

            }

            override fun onCardAppeared(view: View?, position: Int) {

            }

            override fun onCardDisappeared(view: View?, position: Int) {

            }

        })

        cardStackAdapter = CardStackAdapter(baseContext, usersDataList)
        cardStackView.layoutManager = manager
        cardStackView.adapter = cardStackAdapter

//        getUserDataList()
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
                        usersDataList.add(user!!)
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

    private fun userLikeOtherUser(myUid: String, otherUid: String) {

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
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //여기 리스트 안에서 나의 uid가 있는지 확인
                //내가 좋아요를 누른 사람의 좋아요 리스트를 불러온다
                //상대 좋아요 리스트에서 내 uid가 있는지 확인
                for (dataModel in dataSnapshot.children) {

                    val likeUserKey = dataModel.key.toString()
                    if (likeUserKey.equals(uid)) {
                        Toast.makeText(this@MainActivity, "매칭 완료", Toast.LENGTH_SHORT).show()
                        createNotificationChannel()
                        sendNotification()
                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.myLikeRef.child(otherUid).addValueEventListener(postListener)
    }
    //Notification

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Test_Channel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

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
}

