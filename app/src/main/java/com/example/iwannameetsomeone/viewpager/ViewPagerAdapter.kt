package com.example.iwannameetsomeone.viewpager


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.iwannameetsomeone.fragments.LikeMeFragment
import com.example.iwannameetsomeone.fragments.MsgFragment
import com.example.iwannameetsomeone.fragments.MyLikeFragment

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    //탭 총 개수
    override fun getItemCount(): Int {
        return 3
    }
    //포지션 선택에 따른 프래그먼트
    override fun createFragment(position: Int): Fragment {
        var getFragment:Fragment? = null
        when (position) {
            0 -> getFragment = MyLikeFragment()
            1 -> getFragment =  LikeMeFragment()
            2 -> getFragment =  MsgFragment()

        }
        return getFragment!!
    }
}