package com.example.iwannameetsomeone.viewpager


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.iwannameetsomeone.fragments.LikeMeFragment
import com.example.iwannameetsomeone.fragments.MyLikeFragment

private const val NUM_TABS = 2

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        var getFragment:Fragment? = null
        when (position) {
            0 -> getFragment = MyLikeFragment()
            1 -> getFragment =  LikeMeFragment()

        }
        return getFragment!!
    }
}