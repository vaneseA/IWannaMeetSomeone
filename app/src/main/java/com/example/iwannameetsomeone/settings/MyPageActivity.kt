package com.example.iwannameetsomeone.settings

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.iwannameetsomeone.Adapter.ListViewAdapter
import com.example.iwannameetsomeone.Message.MsgModel
import com.example.iwannameetsomeone.Message.fcm.NotiModel
import com.example.iwannameetsomeone.Message.fcm.PushNotification
import com.example.iwannameetsomeone.Message.fcm.RetrofitInstance
import com.example.iwannameetsomeone.R
import com.example.iwannameetsomeone.auth.UserDataModel

import com.example.iwannameetsomeone.utils.FirebaseAuthUtils
import com.example.iwannameetsomeone.utils.FirebaseRef
import com.example.iwannameetsomeone.utils.MyInfo
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_my_page.*
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.item_card.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*

private val uid = FirebaseAuthUtils.getUid()
private val childUid = FirebaseAuthUtils.getUid()

private val TAG = "MyPageActivity"
private val likeUserListUid = mutableListOf<String>()
private val likeUserList = mutableListOf<UserDataModel>()

private var birth = Date()
private var y = 0
private var m = 0
private var d = 0

lateinit var listviewAdapter: ListViewAdapter
lateinit var getterUid: String
lateinit var getterToken: String

class MyPageActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        getMyData()
        val userListView = findViewById<ListView>(R.id.PepleWhoLikesMeListView)

        listviewAdapter = ListViewAdapter(this, likeUserList)
        userListView.adapter = listviewAdapter


        // 내가 좋아요한 사람들
        getMyLikeList()

        // 전체 유저 중에서, 내가 좋아요한 사람들 가져와서
        // 이 사람이 나와 매칭이 되어있는지 확인

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

        userListView.setOnItemClickListener { parent, view, position, id ->

//            Log.d(TAG, likeUserList[position].uid.toString())
            checkMatching(likeUserList[position].uid.toString())
            getterUid = likeUserList[position].uid.toString()
            getterToken = likeUserList[position].token.toString()

            return@setOnItemClickListener
        }

        userListView.setOnItemLongClickListener { parent, view, position, id ->


            FirebaseRef.userLikeRef.child(uid).child(childUid).removeValue()

            return@setOnItemLongClickListener (true)
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
                genderCheck,
                myLocation.text.toString()
            )
            uploadImageForUpdate(uid)
            finish()
        }

    }

    private fun getMyData() {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, dataSnapshot.toString())
                val data = dataSnapshot.getValue(UserDataModel::class.java)

//                myMessage.text = data!!.uid
                myNickname.setText(data!!.nickname)
                myBirth.setText(data!!.birth)
                myLocation.setText(data!!.location)

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

    private fun checkMatching(otherUid: String) {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                Log.d(TAG, otherUid)
                Log.e(TAG, dataSnapshot.toString())

                if (dataSnapshot.children.count() == 0) {

                    Toast.makeText(this@MyPageActivity, "상대방이 좋아요한 사람이 아무도 없어요.", Toast.LENGTH_LONG)
                        .show()

                } else {

                    for (dataModel in dataSnapshot.children) {

                        val likeUserKey = dataModel.key.toString()
                        if (likeUserKey.equals(uid)) {
                            Toast.makeText(this@MyPageActivity, "매칭이 되었습니다.", Toast.LENGTH_LONG)
                                .show()
                            showDialog()
                        } else {
                            Toast.makeText(this@MyPageActivity, "매칭이 되지 않았습니다.", Toast.LENGTH_LONG)
                                .show()
                        }

                    }

                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(otherUid).addValueEventListener(postListener)

    }

    private fun getMyLikeList() {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children) {
                    // 내가 좋아요 한 사람들의 uid가  likeUserList에 들어있음
                    likeUserListUid.add(dataModel.key.toString())
                }
                getUserDataList()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(uid).addValueEventListener(postListener)

    }

    private fun getUserDataList() {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children) {

                    val user = dataModel.getValue(UserDataModel::class.java)

                    // 전체 유저중에 내가 좋아요한 사람들의 정보만 add함
                    if (likeUserListUid.contains(user?.uid)) {

                        likeUserList.add(user!!)
                    }

                }
                listviewAdapter.notifyDataSetChanged()
                Log.d(TAG, likeUserList.toString())

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.addValueEventListener(postListener)

    }

    //PUSH
    private fun testPush(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {

        RetrofitInstance.api.postNotification(notification)

    }


    // Dialog
    private fun showDialog() {

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("메세지 보내기")

        val mAlertDialog = mBuilder.show()

        val btn = mAlertDialog.findViewById<Button>(R.id.sendBtnArea)
        val textArea = mAlertDialog.findViewById<EditText>(R.id.sendTextArea)
        btn?.setOnClickListener {

            val msgText = textArea!!.text.toString()

            val mgsModel = MsgModel(MyInfo.myNickname, msgText)

            FirebaseRef.userMsgRef.child(getterUid).push().setValue(mgsModel)

            val notiModel = NotiModel(MyInfo.myNickname, msgText)

            val pushModel = PushNotification(notiModel, getterToken)

            testPush(pushModel)

            mAlertDialog.dismiss()
        }

        // message
        // 받는 사람 uid
        // Message
        // 누가 보냈는지

    }

    private fun userLikeCansle(myUid: String, otherUid: String) {


        FirebaseRef.userLikeRef.child(myUid).child(otherUid).removeValue()

    }

    private fun updateUserData(
        uid: String,
        nickname: String,
        birth: String,
        gender: String,
        location: String
    ) {
        val genderCheck = if (rbProfileSet_Male.isChecked) "남자" else "여자"
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        val userInfo =
            UserDataModel(
                dbRef.key.toString(),
                myNickname.text.toString(),
                myBirth.text.toString(),
                genderCheck,
                myLocation.text.toString()
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
                birthTxt!!.text = "$y.$m.$d"
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

}