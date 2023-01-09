package com.example.iwannameetsomeone.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iwannameetsomeone.Adapter.MyLikeRVAdapter
import com.example.iwannameetsomeone.R
import com.example.iwannameetsomeone.auth.MainViewModel
import com.example.iwannameetsomeone.auth.UserDataModel
import com.example.iwannameetsomeone.databinding.FragmentMyLikeBinding
import com.example.iwannameetsomeone.utils.FirebaseAuthUtils
import com.example.iwannameetsomeone.utils.FirebaseRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class MyLikeFragment : Fragment() {

    private val uid = FirebaseAuthUtils.getUid()

    private val TAG = "MyLikeFragment"

    private var _binding: FragmentMyLikeBinding? = null
    private val binding get() = _binding!!

//    private val myLikeList = ArrayList<InterestCoinEntity>()

    private val likeUserListUid = mutableListOf<String>()
    private val likeUserList = mutableListOf<UserDataModel>()

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


        val myLikeLRVAdapter = MyLikeRVAdapter(requireContext(), likeUserList)
        binding.myLikeRV.adapter = myLikeLRVAdapter
        binding.myLikeRV.layoutManager = LinearLayoutManager(requireContext())

        myLikeLRVAdapter.itemClick = object : MyLikeRVAdapter.ItemClick {
            override fun onClick(view: View, position: Int) {

                val postListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        for (dataModel in dataSnapshot.children) {

                            val user = dataModel.getValue(UserDataModel::class.java)

                            // 전체 유저중에 내가 좋아요한 사람들의 정보만 add함
                            if (likeUserListUid.contains(user?.uid)) {

                                likeUserList.add(user!!)
                            }

                        }
                        myLikeLRVAdapter.notifyDataSetChanged()
                        Log.d(TAG, likeUserList.toString())

                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Getting Post failed, log a message
                        Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                    }
                }
                FirebaseRef.userInfoRef.addValueEventListener(postListener)


            }
//        FirebaseRef.userLikeRef
//        viewModel.getAllInterestCoinData()
//        viewModel.myLikeList.observe(viewLifecycleOwner, Observer {
//
//            likeUserList.clear()
//
//            for (item in it) {
//
//                if (item.selected) {
//                    likeUserList.add(item)
//                } else {
//                    unSelectedList.add(item)
//                }
//
//            }
//
//
//            myLikeListRV()
//
//        })
//    }

//            private fun myLikeListRV() {
//
//                val myLikeLRVAdapter = MyLikeRVAdapter(requireContext(), likeUserList)
//                binding.myLikeRV.adapter = myLikeLRVAdapter
//                binding.myLikeRV.layoutManager = LinearLayoutManager(requireContext())
//
//                myLikeLRVAdapter.itemClick = object : MyLikeRVAdapter.ItemClick {
//                    override fun onClick(view: View, position: Int) {
//
//                        getUserDataList()
//
//                    }
                }
  }


            override fun onDestroy() {
                super.onDestroy()
                _binding = null
            }

        }
