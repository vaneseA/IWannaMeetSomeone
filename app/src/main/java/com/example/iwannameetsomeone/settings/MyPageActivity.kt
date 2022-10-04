package com.example.iwannameetsomeone.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.RadioGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.iwannameetsomeone.Adapter.ListViewAdapter
import com.example.iwannameetsomeone.R
import com.example.iwannameetsomeone.auth.UserDataModel
import com.example.iwannameetsomeone.utils.FirebaseAuthUtils
import com.example.iwannameetsomeone.utils.FirebaseRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_my_page.*
import kotlinx.android.synthetic.main.item_card.*

private val uid = FirebaseAuthUtils.getUid()

private val TAG = "MyPageActivity"
private val likeUserListUid = mutableListOf<String>()
private val likeUserList = mutableListOf<UserDataModel>()

lateinit var listviewAdapter: ListViewAdapter

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

        // 제가 하고 싶은것은
        // 전체 유저 중에서, 내가 좋아요한 사람들 가져와서
        // 이 사람이 나와 매칭이 되어있는지 확인하는 것!!

        userListView.setOnItemClickListener { parent, view, position, id ->

//            Log.d(TAG, likeUserList[position].uid.toString())
            checkMatching(likeUserList[position].uid.toString())
        }

    }

    private fun getMyData() {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, dataSnapshot.toString())
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                myMessage.text = data!!.uid
                myNickname.text = data!!.nickname
                myBirth.text = data!!.birth.toString()
                myLocation.text = data!!.location

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
                            .into(myProfileImg)

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

                    Toast.makeText(this@MyPageActivity, "매칭이 되지 않았습니다.", Toast.LENGTH_LONG).show()

                } else {

                    for (dataModel in dataSnapshot.children) {

                        val likeUserKey = dataModel.key.toString()
                        if (likeUserKey.equals(uid)) {
                            Toast.makeText(this@MyPageActivity, "매칭이 되었습니다.", Toast.LENGTH_LONG)
                                .show()
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
}


//    private fun getMyLikeList(){
//
//        val postListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//
//                for (dataModel in dataSnapshot.children) {
//                    // 내가 좋아요 한 사람들의 uid가  likeUserList에 들어있음
//                    likeUserListUid.add(dataModel.key.toString())
//                }
//                getUserDataList()
//
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
//            }
//        }
//        FirebaseRef.userLikeRef.child(uid).addValueEventListener(postListener)
//
//    }

//    private fun getUserDataList(){
//
//        val postListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//
//                for (dataModel in dataSnapshot.children) {
//
//                    val user = dataModel.getValue(UserDataModel::class.java)
//
//                    // 전체 유저중에 내가 좋아요한 사람들의 정보만 add함
//                    if(likeUserListUid.contains(user?.uid)) {
//
//                        likeUserList.add(user!!)
//                    }
//
//                }
//                listviewAdapter.notifyDataSetChanged()
//                Log.d(TAG, likeUserList.toString())
//
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
//            }
//        }
//        FirebaseRef.userInfoRef.addValueEventListener(postListener)
//
//    }

