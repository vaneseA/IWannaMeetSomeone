package com.example.iwannameetsomeone.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.iwannameetsomeone.Adapter.MsgLVAdapter
import com.example.iwannameetsomeone.Message.MsgModel
import com.example.iwannameetsomeone.Message.fcm.NotiModel
import com.example.iwannameetsomeone.Message.fcm.PushNotification
import com.example.iwannameetsomeone.Message.fcm.RetrofitInstance
import com.example.iwannameetsomeone.R
import com.example.iwannameetsomeone.auth.UserDataModel
import com.example.iwannameetsomeone.databinding.FragmentMsgBinding
import com.example.iwannameetsomeone.settings.getterToken
import com.example.iwannameetsomeone.settings.getterUid
import com.example.iwannameetsomeone.utils.FirebaseAuthUtils
import com.example.iwannameetsomeone.utils.FirebaseRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.custom_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


// 태그
private val TAG: String = "MsgFragment"

// 메시지 어댑터
lateinit var msgListviewAdapter: MsgLVAdapter

val msgList = mutableListOf<MsgModel>()

class MsgFragment : Fragment() {

    private val uid = FirebaseAuthUtils.getUid()

    private var _binding: FragmentMsgBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMsgBinding.inflate(inflater, container, false)
        val view = binding.root

        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val msgListView = binding.msgListView
        msgListviewAdapter = MsgLVAdapter(requireContext(), msgList)
        msgListView.adapter = msgListviewAdapter
        getMyMsg()

        msgListView.setOnItemClickListener { parent, view, position, id ->
            getterUid = msgList[position].uid
            getterToken = msgList[position].token
            showDialog()

        }
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
                for (dataModel in dataSnapshot.children) {

                    // 메시지 모델에서
                    val msg = dataModel.getValue(MsgModel::class.java)
                    msgList.add(msg!!)
                    Log.d(TAG, msg.toString())

                }

                // 역순 정렬
                msgList.reverse()

                // 동기화(새로고침) -> 리스트 크기 및 아이템 변화를 어댑터에 알림
                msgListviewAdapter.notifyDataSetChanged()

            }

            // 실패
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "getMyMsg - loadPost:onCancelled", databaseError.toException())
            }

        }

        // 파이어베이스 내 데이터의 변화(추가)를 알려줌
        FirebaseRef.userMsgRef.child(FirebaseAuthUtils.getUid()).addValueEventListener(postListener)

    }

    // MSG Dialog
    private fun showDialog() {

        val allUid = FirebaseDatabase.getInstance().reference
        allUid.get().addOnSuccessListener {

            val map = it.child("users").child(getterUid).getValue() as HashMap<String, Any>
            val map2 = it.child("users").child(uid).getValue() as HashMap<String, Any>

            val toName = map.get("nickname").toString()
            val fromName = map2.get("nickname").toString()

            val mDialogView =
                LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog, null)
            val mBuilder = AlertDialog.Builder(requireContext())
                .setView(mDialogView)


            val mAlertDialog = mBuilder.show()

            mAlertDialog.toNick.text = "$toName" + "님에게 메세지를 보냅니다."
            mAlertDialog.fromNick.text = "from: $fromName"
            val btn = mAlertDialog.findViewById<Button>(R.id.sendBtnArea)
            val textArea = mAlertDialog.findViewById<EditText>(R.id.sendTextArea)

            mAlertDialog.dialogBackBtn.setOnClickListener {
                mAlertDialog.dismiss()
            }

            btn?.setOnClickListener {

                val msgText = textArea!!.text.toString()

                val msgModel = MsgModel(fromName, msgText, uid)

                FirebaseRef.userMsgRef.child(getterUid).push().setValue(msgModel)

                val notiModel = NotiModel(fromName, msgText)

                val pushModel = PushNotification(notiModel, getterToken)

                testPush(pushModel)

                Toast.makeText(requireContext(), "메세지를 전송헀습니다", Toast.LENGTH_LONG)
                    .show()
                mAlertDialog.dismiss()

            }
        }
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

}

