package com.example.iwannameetsomeone.Message

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import com.example.iwannameetsomeone.R
import com.example.iwannameetsomeone.settings.listviewAdapter
import com.example.iwannameetsomeone.utils.FirebaseAuthUtils
import com.example.iwannameetsomeone.utils.FirebaseRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_my_msg.*

// 태그
private val TAG: String = "MyMsgActivity"

// 메시지 어댑터
lateinit var msgListviewAdapter : MsgAdapter

val msgList = mutableListOf<MsgModel>()

class MyMsgActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_msg)

        val msgListView = findViewById<ListView>(R.id.msgListView)


        msgListviewAdapter = MsgAdapter(this, msgList)
        msgListView.adapter = msgListviewAdapter

        getMyMsg()

        messgeBoxBackBtn.setOnClickListener { finish() }
    }


    // 내 메시지 불러오기
    private fun getMyMsg() {

        // 데이터베이스에서 컨텐츠의 세부정보를 검색
        val postListener = object : ValueEventListener {

            // 데이터 스냅샷
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                // 중복 출력 방지 위해 메시지 목록 비워줌
                msgList.clear()

                // 데이터스냅샷 내 사용자 데이터 출력
                for(dataModel in dataSnapshot.children) {

                    // 메시지 모델에서
                    val msg = dataModel.getValue(MsgModel::class.java)
                    msgList.add(msg!!)
                    Log.d(TAG, msg.toString())

                }

                // 역순 정렬
                msgList.reverse()

                // 동기화(새로고침) -> 리스트 크기 및 아이템 변화를 어댑터에 알림
                listviewAdapter.notifyDataSetChanged()

            }

            // 실패
            override fun onCancelled(databaseError: DatabaseError) { Log.w(TAG, "getMyMsg - loadPost:onCancelled", databaseError.toException()) }

        }

        // 파이어베이스 내 데이터의 변화(추가)를 알려줌
        FirebaseRef.userMsgRef.child(FirebaseAuthUtils.getUid()).addValueEventListener(postListener)

    }
}