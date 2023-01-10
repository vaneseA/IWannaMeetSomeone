package com.example.iwannameetsomeone.fragments

import android.annotation.SuppressLint
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
import com.example.iwannameetsomeone.Adapter.MyLikeLVAdapter
import com.example.iwannameetsomeone.Message.MsgModel
import com.example.iwannameetsomeone.Message.fcm.NotiModel
import com.example.iwannameetsomeone.Message.fcm.PushNotification
import com.example.iwannameetsomeone.Message.fcm.RetrofitInstance
import com.example.iwannameetsomeone.R
import com.example.iwannameetsomeone.auth.UserDataModel
import com.example.iwannameetsomeone.databinding.FragmentLikeMeBinding
import com.example.iwannameetsomeone.settings.getterToken
import com.example.iwannameetsomeone.settings.getterUid
import com.example.iwannameetsomeone.utils.FirebaseAuthUtils
import com.example.iwannameetsomeone.utils.FirebaseRef
import com.example.iwannameetsomeone.utils.MyInfo
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.custom_dialog.*
import kotlinx.android.synthetic.main.custom_mylike_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LikeMeFragment : Fragment() {

    private val usersDataList = mutableListOf<UserDataModel>()
    // 사용자 수 세기
    private var userCount = 0

    private val uid = FirebaseAuthUtils.getUid()

    private val TAG = "LikeMeFragment"

    private var _binding: FragmentLikeMeBinding? = null
    private val binding get() = _binding!!
    lateinit var listviewAdapter: MyLikeLVAdapter


    private val userLikeMeListUid = mutableListOf<String>()
    private val userLikeMeList = mutableListOf<UserDataModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLikeMeBinding.inflate(inflater, container, false)
        val view = binding.root

        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val userListView = binding.PepleWhoLikesMeListView

        listviewAdapter = MyLikeLVAdapter(requireContext(), userLikeMeList)
        userListView.adapter = listviewAdapter
        getUserLikeMeList()
        userListView.setOnItemClickListener { parent, view, position, id ->
            getterUid = userLikeMeList[position].uid.toString()

            val allUid = FirebaseDatabase.getInstance().reference

            allUid.get().addOnSuccessListener {
                val map = it.child("users").child(getterUid).getValue() as HashMap<String, Any>
                val name = map.get("nickname").toString()
                val age = map.get("age").toString()
                val location = map.get("location").toString()

                val mtDialogView =
                    LayoutInflater.from(requireContext())
                        .inflate(R.layout.custom_likeme_dialog, null)
                val mtBuilder = AlertDialog.Builder(requireContext())
                    .setView(mtDialogView)

                val mtAlertDialog = mtBuilder.show()

                mtAlertDialog.dialogNickname.text = name
                mtAlertDialog.dialogAge.text = ", " + age
                mtAlertDialog.dialogLocation.text = location

                val storageRef = Firebase.storage.reference.child(getterUid + ".png")
                storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->

                    if (task.isSuccessful) {
                        Glide.with(requireContext())
                            .load(task.result)
                            .into(mtAlertDialog.dialogProfileImageArea)

                    }

                })
                mtAlertDialog.messageBtn.setOnClickListener {
                    checkMatching(getterUid)
                    getterUid =
                        userLikeMeList[position].uid.toString()
                    getterToken =
                        userLikeMeList[position].token.toString()
                }
                mtAlertDialog.profileDialogBackBtn.setOnClickListener {
                    mtAlertDialog.dismiss()
                }
                mtAlertDialog.cancelBtnArea.setOnClickListener {
                    userLikeOtherUser(uid, usersDataList[userCount].uid.toString())
                    userLikeCansle(uid, getterUid)
                    mtAlertDialog.dismiss()
                    Toast.makeText(requireContext(), "나도 좋아요를 눌습니다.", Toast.LENGTH_LONG)
                        .show()
                }
            }



            return@setOnItemClickListener
        }
    }

    private fun checkMatching(otherUid: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("dd", uid)

                if (dataSnapshot.children.count() == 0) {

                    Toast.makeText(requireContext(), "메시지를 보낼 수 없습니다", Toast.LENGTH_LONG)
                        .show()

                } else {
                    var check = false
                    // 데이터스냅샷 내 사용자 데이터 출력
                    for (dataModel in dataSnapshot.children) {

                        // 다른 사용자가 좋아요 한 사용자 목록에
                        val likeUserKey = dataModel.key.toString()

                        // 현재 사용자가 포함돼 있으면
                        if (likeUserKey == uid) {

                            Toast.makeText(requireContext(), "메세지를 보낼 수 있습니다", Toast.LENGTH_LONG)
                                .show()
                            // 메시지 입력창 띄움
                            check = true
                            showDialog()
                            break
                        }
                    }
                    if (check == false) {

                        Toast.makeText(
                            requireContext(),
                            "매칭되지 않아 메세지를 보낼 수 없습니다",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }

                }


            }


            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(
                    TAG,
                    "loadPost:onCancelled",
                    databaseError.toException()
                )
            }
        }
        FirebaseRef.myLikeRef.child(otherUid).addValueEventListener(postListener)

    }

    private fun getUserLikeMeList() {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                      게시글 목록 비움
//                      -> 저장/삭제 마다 데이터 누적돼 게시글 중복으로 저장되는 것 방지
                userLikeMeList.clear()
                for (dataModel in dataSnapshot.children) {
                    // 내가 좋아요 한 사람들의 uid가  likeUserList에 들어있음
                    userLikeMeListUid.add(dataModel.key.toString())
                }

                listviewAdapter.notifyDataSetChanged()

                getUserDataList()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(
                    TAG,
                    "loadPost:onCancelled",
                    databaseError.toException()
                )
            }
        }
//        val likeMeRef = FirebaseRef.database.getReference("LikeMe")
        FirebaseRef.likeMeRef.child(uid)
            .addValueEventListener(postListener)

    }

    private fun getUserDataList() {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children) {

                    val user = dataModel.getValue(UserDataModel::class.java)

                    // 전체 유저중에 내가 좋아요한 사람들의 정보만 add함
                    if (userLikeMeListUid.contains(user?.uid)) {

                        userLikeMeList.add(user!!)
                    }

                }
                listviewAdapter.notifyDataSetChanged()
                Log.d(
                    TAG,
                    userLikeMeList.toString()
                )

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(
                    TAG,
                    "loadPost:onCancelled",
                    databaseError.toException()
                )
            }
        }
        FirebaseRef.userInfoRef.addValueEventListener(postListener)

    }

    // Dialog
    private fun showDialog() {
        val allUid = FirebaseDatabase.getInstance().reference
        allUid.get().addOnSuccessListener {
            val map = it.child("users").child(getterUid).getValue() as HashMap<String, Any>
            val name = map.get("nickname").toString()

            val mDialogView =
                LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog, null)
            val mBuilder = AlertDialog.Builder(requireContext())
                .setView(mDialogView)


            val mAlertDialog = mBuilder.show()

            mAlertDialog.toNick.text = "$name" + "님에게 메세지를 보냅니다."

            val btn = mAlertDialog.findViewById<Button>(R.id.sendBtnArea)
            val textArea = mAlertDialog.findViewById<EditText>(R.id.sendTextArea)

            mAlertDialog.dialogBackBtn.setOnClickListener { mAlertDialog.dismiss() }

            btn?.setOnClickListener {

                val msgText = textArea!!.text.toString()

//            val senderInfo =


                val msgModel = MsgModel(MyInfo.myNickname, msgText)

                FirebaseRef.userMsgRef.child(getterUid).push().setValue(msgModel)

                val notiModel = NotiModel(MyInfo.myNickname, msgText)

                val pushModel = PushNotification(notiModel, getterToken)

                testPush(pushModel)

                Toast.makeText(requireContext(), "메세지를 전송헀습니다", Toast.LENGTH_LONG)
                    .show()
                mAlertDialog.dismiss()

            }
        }
    }

    private fun userLikeCansle(myUid: String, otherUid: String) {


        FirebaseRef.myLikeRef.child(myUid).child(otherUid).removeValue()

    }

    //PUSH
    private fun testPush(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {

            RetrofitInstance.api.postNotification(notification)

        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun userLikeOtherUser(myUid: String, otherUid: String) {


        FirebaseRef.myLikeRef.child(myUid).child(otherUid).setValue("true")

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

            }
            //실패시
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // 파이어베이스 내 데이터의 변화(추가)를 알려줌
        FirebaseRef.userInfoRef.addValueEventListener(postListener)

    }
}
