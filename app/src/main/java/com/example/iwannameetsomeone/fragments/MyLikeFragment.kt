package com.example.iwannameetsomeone.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.iwannameetsomeone.Adapter.LikeLVAdapter
import com.example.iwannameetsomeone.MainActivity
import com.example.iwannameetsomeone.Message.MsgModel
import com.example.iwannameetsomeone.Message.fcm.NotiModel
import com.example.iwannameetsomeone.Message.fcm.PushNotification
import com.example.iwannameetsomeone.Message.fcm.RetrofitInstance
import com.example.iwannameetsomeone.R
import com.example.iwannameetsomeone.auth.UserDataModel
import com.example.iwannameetsomeone.databinding.FragmentMyLikeBinding
import com.example.iwannameetsomeone.settings.MyPageActivity
import com.example.iwannameetsomeone.settings.getterToken
import com.example.iwannameetsomeone.settings.getterUid
import com.example.iwannameetsomeone.utils.FirebaseAuthUtils
import com.example.iwannameetsomeone.utils.FirebaseRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.custom_likeme_dialog.*
import kotlinx.android.synthetic.main.custom_msg_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MyLikeFragment : Fragment() {


    //Uid
    private val uid = FirebaseAuthUtils.getUid()

    //Token
    private val token = ""

    //TAG
    private val TAG = "MyLikeFragment"

    // (전역변수) 바인딩 객체 선언
    private var vBinding: FragmentMyLikeBinding? = null

    // 매번 null 확인 귀찮음 -> 바인딩 변수 재선언
    private val binding get() = vBinding!!

    // 리스트뷰 어댑터 선언
    lateinit var listviewAdapter: LikeLVAdapter

    // 유저데이터 리스트 목록
    private val MyLikeUserList = mutableListOf<UserDataModel>()

    // 내가 좋아요를 누른 유저 UID 목록을 확인을 위한 목록
    private val MyLikeUserListUid = mutableListOf<String>()

    //모든 유저의 UID
    val allUid = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 뷰바인딩
        vBinding = FragmentMyLikeBinding.inflate(inflater, container, false)
        val view = binding.root

        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 유저 리스트 뷰
        val userListView = binding.PeopleWhoMyLikeListView


        // 리스트뷰 어댑터 연결(유저 데이터 리스트)
        listviewAdapter = LikeLVAdapter(requireContext(), MyLikeUserList)

        // 리스트뷰 어댑터 연결
        userListView.adapter = listviewAdapter

        //내가 좋아요릃 누른 유저목록을 불러옴
        getMyLikeList()

        //리스트의 Item 클릭시
        userListView.setOnItemClickListener { parent, view, position, id ->

            //선택한 유저의 UID를 받아옴
            getterUid = MyLikeUserList[position].uid.toString()

            //선택한 유저의 Token을 받아옴
            getterToken = MyLikeUserList[position].token.toString()

            //모든 유저의 UID를 성공적으로 받아왔을 때
            allUid.get().addOnSuccessListener {

                //HashMap으로 다른 유저의 UID를 가져옴
                val map = it.child("users").child(getterUid).getValue() as HashMap<String, Any>

                //map을 이용해 다른 유저의 닉네임을 받아옴
                val name = map.get("nickname").toString()

                //map을 이용해 다른 유저의 나이를 받아옴
                val age = map.get("age").toString()

                //map을 이용해 다른 유저의 직업을 받아옴
                val job = map.get("job").toString()

                //map을 이용해 다른 유저의 지역을 받아옴
                val location = map.get("location").toString()

                //다이얼로그 뷰
                val mtDialogView =
                    LayoutInflater.from(requireContext())
                        .inflate(R.layout.custom_mylike_dialog, null)
                val mtBuilder = AlertDialog.Builder(requireContext())
                    .setView(mtDialogView)

                val mtAlertDialog = mtBuilder.show()

                //다이얼로그 상의 정보를 Map의 정보를 받아와 입력
                mtAlertDialog.dialogNickname.text = name
                mtAlertDialog.dialogAge.text = ", " + age
                mtAlertDialog.dialogJob.text = job
                mtAlertDialog.dialogLocation.text = location

                //파이어베이스 스토리지에 있는 이미지를 getterUid + ".png"로 받아옴
                val storageRef = Firebase.storage.reference.child(getterUid + ".png")
                storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->

                    //성공적으로 데이터를 받아올 경우 Glide를 통해 dialogProfileImageArea에 넣음
                    if (task.isSuccessful) {
                        Glide.with(requireContext())
                            .load(task.result)
                            .into(mtAlertDialog.dialogProfileImageArea)
                    }
                })
                //버튼을 누르면 메세지를 보낼수있는 메세지 다이얼로그를 불러옴
                mtAlertDialog.messageBtn.setOnClickListener {
                    //매칭이 되었는지 체크 후 메세지 다이얼로그를 불러옴
                    checkMatching(getterUid)
                }

                //다이얼로그를 닫는 버튼
                mtAlertDialog.profileDialogBackBtn.setOnClickListener {
                    mtAlertDialog.dismiss()
                }
                //좋아요를 취소하는 버튼
                mtAlertDialog.likeTooBtnArea.setOnClickListener {
                    userLikeCansle(uid, getterUid)
                    mtAlertDialog.dismiss()
                    Toast.makeText(requireContext(), "좋아요를 취소했습니다.", Toast.LENGTH_LONG)
                        .show()
                    // 좋아요 취소시 Fragment 목록이 갱신이 언되는 문제 발생
                    // 임시방편으로 메인으로 나갔다가 다시 마이페이지로 이동하도록 함
                    // 차후 수정 예정
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    startActivity(Intent(requireContext(), MyPageActivity::class.java))
                }
            }
            return@setOnItemClickListener
        }
    }
    //매칭되었는지 확인하는 함수
    private fun checkMatching(otherUid: String) {

        // 데이터베이스에서 컨텐츠의 세부정보를 검색
        val postListener = object : ValueEventListener {

            // 데이터스냅샷 내 사용자 데이터 출력
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.children.count() == 0) {//나에게 좋아요를 부른 사람이 0일 때 메세지를 보낼수 없게
                    Toast.makeText(requireContext(), "메시지를 보낼 수 없습니다", Toast.LENGTH_LONG)
                        .show()
                } else {

                    //매칭되었는지 확인하는 변수
                    var check = false

                    // 데이터스냅샷 내 사용자 데이터 출력
                    for (dataModel in dataSnapshot.children) {

                        // 다른 사용자가 좋아요 한 사용자 목록에
                        val likeUserKey = dataModel.key.toString()

                        // 현재 사용자가 포함돼 있으면
                        if (likeUserKey == uid) {

                            Toast.makeText(requireContext(), "메세지를 보낼 수 있습니다", Toast.LENGTH_LONG)
                                .show()
                            // 현재 사용자가 포함돼 있으면 true로 변경
                            check = true

                            // 메시지 입력창 띄움
                            showDialogForMsg()

                            //브레이크
                            break
                        }
                    }
                    if (check == false) {//매칭되지 않았을 때

                        Toast.makeText(
                            requireContext(),
                            "매칭되지 않아 메세지를 보낼 수 없습니다",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
            }
            //실패시
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // 파이어베이스 내 데이터의 변화(추가)를 알려줌
        FirebaseRef.myLikeRef.child(otherUid).addValueEventListener(postListener)

    }
    //내가 좋아요를 누른 유저리스트를 받아옴
    private fun getMyLikeList() {

        // 데이터베이스에서 컨텐츠의 세부정보를 검색
        val postListener = object : ValueEventListener {

            // 데이터스냅샷 내 사용자 데이터 출력
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                // 목록을 비움-> 저장/삭제 마다 데이터 누적돼 게시글 중복으로 저장되는 것 방지
                MyLikeUserList.clear()

                for (dataModel in dataSnapshot.children) {
                    // 내가 좋아요 한 사람들의 uid가  likeUserList에 들어있음
                    MyLikeUserListUid.add(dataModel.key.toString())
                }
                // 동기화(새로고침) -> 리스트 크기 및 아이템 변화를 어댑터에 알림
                listviewAdapter.notifyDataSetChanged()

                //유저 데이터 리스트를 받아옴
                getUserDataList()
            }

            //실패시
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // 파이어베이스 내 데이터의 변화(추가)를 알려줌
        FirebaseRef.myLikeRef.child(uid)
            .addValueEventListener(postListener)

    }
    // 전체 사용자 정보
    private fun getUserDataList() {


        // 데이터베이스에서 컨텐츠의 세부정보를 검색
        val postListener = object : ValueEventListener {

            // 데이터스냅샷 내 사용자 데이터 출력
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children) {
                    val user = dataModel.getValue(UserDataModel::class.java)
                    // 전체 유저중에 내가 좋아요한 사람들의 정보만 add함
                    if (MyLikeUserListUid.contains(user?.uid)) {
                        MyLikeUserList.add(user!!)
                    }
                }
                // 동기화(새로고침) -> 리스트 크기 및 아이템 변화를 어댑터에 알림
                listviewAdapter.notifyDataSetChanged()
            }
            //실패시
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // 파이어베이스 내 데이터의 변화(추가)를 알려줌
        FirebaseRef.userInfoRef.addValueEventListener(postListener)

    }

    // 메세지 다이얼로그
    private fun showDialogForMsg() {

        //모든 유저의 UID를 성공정우를 받아올 경우
        allUid.get().addOnSuccessListener {

            //HashMap으로 다른 유저의 UID를 가져옴
            val map = it.child("users").child(getterUid).getValue() as HashMap<String, Any>

            //HashMap으로 내 UID를 가져옴
            val map2 = it.child("users").child(uid).getValue() as HashMap<String, Any>

            //map을 이용해 다른 유저의 닉네임을 받아옴
            val toName = map.get("nickname").toString()

            //map을 이용해 내 닉네임을 받아옴
            val fromName = map2.get("nickname").toString()


            //다이얼로그 뷰
            val mDialogView =
                LayoutInflater.from(requireContext()).inflate(R.layout.custom_msg_dialog, null)
            val mBuilder = AlertDialog.Builder(requireContext())
                .setView(mDialogView)

            val mAlertDialog = mBuilder.show()

            //다이얼로그 상의 정보를 Map의 정보를 받아와 입력
            mAlertDialog.toNick.text = "$toName" + "님에게 메세지를 보냅니다."
            mAlertDialog.fromNick.text = "from: $fromName"

            //custom_msg_dialog의 ID 정의
            val btn = mAlertDialog.findViewById<Button>(R.id.sendBtnArea)
            val textArea = mAlertDialog.findViewById<EditText>(R.id.sendTextArea)


            //다이얼로그를 닫는 버튼
            mAlertDialog.dialogBackBtn.setOnClickListener { mAlertDialog.dismiss() }

            //메세지 보내기 버튼
            btn?.setOnClickListener {

                //간결하게 입력하기 위해 메세지를 담아둠
                val msgText = textArea!!.text.toString()
                //메세지 모델
                val msgModel = MsgModel(fromName, msgText, uid, token)

                // 파이어베이스에 메시지 업로드
                FirebaseRef.userMsgRef.child(getterUid).push().setValue(msgModel)

                val notiModel = NotiModel(fromName, msgText)
                val pushModel = PushNotification(notiModel, getterToken)

                // 푸시 메시지
                testPush(pushModel)

                //메세지 발신 성공토스트
                Toast.makeText(requireContext(), "메세지를 전송헀습니다", Toast.LENGTH_LONG)
                    .show()

                // 다이얼로그 종료
                mAlertDialog.dismiss()

            }
        }
    }


    //좋아요 취소버튼
    private fun userLikeCansle(myUid: String, otherUid: String) {

        // 파이어베이스에 내 좋아요 목록중 선택한 유저의 UID 삭제
        FirebaseRef.myLikeRef.child(myUid).child(otherUid).removeValue()

    }

    //PUSH
    private fun testPush(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {

            RetrofitInstance.api.postNotification(notification)

        }

}

