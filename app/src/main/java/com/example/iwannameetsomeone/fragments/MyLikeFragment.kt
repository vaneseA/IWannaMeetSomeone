package com.example.iwannameetsomeone.fragments

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
import kotlinx.android.synthetic.main.custom_dialog.*
import kotlinx.android.synthetic.main.custom_likeme_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MyLikeFragment : Fragment() {

    private val uid = FirebaseAuthUtils.getUid()
    private val token = ""
    private val TAG = "MyLikeFragment"

    private var _binding: FragmentMyLikeBinding? = null
    private val binding get() = _binding!!
    lateinit var listviewAdapter: LikeLVAdapter

    private val MyLikeUserListUid = mutableListOf<String>()
    private val MyLikeUserList = mutableListOf<UserDataModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMyLikeBinding.inflate(inflater, container, false)
        val view = binding.root

        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val userListView = binding.PeopleWhoMyLikeListView

        listviewAdapter = LikeLVAdapter(requireContext(), MyLikeUserList)
        userListView.adapter = listviewAdapter





        getMyLikeList()
        userListView.setOnItemClickListener { parent, view, position, id ->
            getterUid = MyLikeUserList[position].uid.toString()

            val allUid = FirebaseDatabase.getInstance().reference

            allUid.get().addOnSuccessListener {
                val map = it.child("users").child(getterUid).getValue() as HashMap<String, Any>
                val name = map.get("nickname").toString()
                val age = map.get("age").toString()
                val location = map.get("location").toString()
                val job = map.get("job").toString()

                val mtDialogView =
                    LayoutInflater.from(requireContext())
                        .inflate(R.layout.custom_mylike_dialog, null)
                val mtBuilder = AlertDialog.Builder(requireContext())
                    .setView(mtDialogView)

                val mtAlertDialog = mtBuilder.show()

                mtAlertDialog.dialogNickname.text = name
                mtAlertDialog.dialogAge.text = ", " + age
                mtAlertDialog.dialogJob.text = job
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
                        MyLikeUserList[position].uid.toString()
                    getterToken =
                        MyLikeUserList[position].token.toString()
                }
                mtAlertDialog.profileDialogBackBtn.setOnClickListener {
                    mtAlertDialog.dismiss()
                }
                mtAlertDialog.likeTooBtnArea.setOnClickListener {
                    userLikeCansle(uid, getterUid)
                    mtAlertDialog.dismiss()
                    Toast.makeText(requireContext(), "???????????? ??????????????????.", Toast.LENGTH_LONG)
                        .show()
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    startActivity(Intent(requireContext(), MyPageActivity::class.java))
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

                    Toast.makeText(requireContext(), "???????????? ?????? ??? ????????????", Toast.LENGTH_LONG)
                        .show()

                } else {
                    var check = false
                    // ?????????????????? ??? ????????? ????????? ??????
                    for (dataModel in dataSnapshot.children) {

                        // ?????? ???????????? ????????? ??? ????????? ?????????
                        val likeUserKey = dataModel.key.toString()

                        // ?????? ???????????? ????????? ?????????
                        if (likeUserKey == uid) {

                            Toast.makeText(requireContext(), "???????????? ?????? ??? ????????????", Toast.LENGTH_LONG)
                                .show()
                            // ????????? ????????? ??????
                            check = true
                            showDialog()
                            break
                        }
                    }
                    if (check == false) {

                        Toast.makeText(
                            requireContext(),
                            "???????????? ?????? ???????????? ?????? ??? ????????????",
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

    private fun getMyLikeList() {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                      ????????? ?????? ??????
//                      -> ??????/?????? ?????? ????????? ????????? ????????? ???????????? ???????????? ??? ??????
                MyLikeUserList.clear()
                for (dataModel in dataSnapshot.children) {
                    // ?????? ????????? ??? ???????????? uid???  likeUserList??? ????????????
                    MyLikeUserListUid.add(dataModel.key.toString())
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
        FirebaseRef.myLikeRef.child(uid)
            .addValueEventListener(postListener)

    }

    private fun getUserDataList() {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children) {

                    val user = dataModel.getValue(UserDataModel::class.java)

                    // ?????? ???????????? ?????? ???????????? ???????????? ????????? add???
                    if (MyLikeUserListUid.contains(user?.uid)) {

                        MyLikeUserList.add(user!!)
                    }

                }
                listviewAdapter.notifyDataSetChanged()
                Log.d(
                    TAG,
                    MyLikeUserList.toString()
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

            mAlertDialog.toNick.text = "$toName" + "????????? ???????????? ????????????."
            mAlertDialog.fromNick.text = "from: $fromName"
            val btn = mAlertDialog.findViewById<Button>(R.id.sendBtnArea)
            val textArea = mAlertDialog.findViewById<EditText>(R.id.sendTextArea)

            mAlertDialog.dialogBackBtn.setOnClickListener { mAlertDialog.dismiss() }

            btn?.setOnClickListener {

                val msgText = textArea!!.text.toString()

                val msgModel = MsgModel(fromName, msgText, uid, token)

                FirebaseRef.userMsgRef.child(getterUid).push().setValue(msgModel)

                val notiModel = NotiModel(fromName, msgText)

                val pushModel = PushNotification(notiModel, getterToken)

                testPush(pushModel)

                Toast.makeText(requireContext(), "???????????? ??????????????????", Toast.LENGTH_LONG)
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


}

