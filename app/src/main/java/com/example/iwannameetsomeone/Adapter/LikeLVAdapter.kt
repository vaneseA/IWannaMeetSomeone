package com.example.iwannameetsomeone.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.iwannameetsomeone.R
import com.example.iwannameetsomeone.auth.UserDataModel

class LikeLVAdapter(val context: Context, val items: MutableList<UserDataModel>) : BaseAdapter() {


    // 리스트 전체 개수
    override fun getCount(): Int = items.size


    // 리스트를 하나씩 가져옴
    override fun getItem(position: Int): Any = items[position]


    // 리스트의 ID를 가져옴
    override fun getItemId(position: Int): Long = position.toLong()


    // 뷰를 꾸며줌
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var convertView = convertView
        if (convertView == null) {

            convertView =
                LayoutInflater.from(parent?.context).inflate(R.layout.my_like_item, parent, false)

        }
        //  닉네임,나이,사는곳
        val nickname = convertView!!.findViewById<TextView>(R.id.listViewItemNickname)
        val age = convertView!!.findViewById<TextView>(R.id.listViewItemAge)
        val location = convertView!!.findViewById<TextView>(R.id.listViewItemLocation)

        // 카드에 넣어줌
        nickname.text = items[position].nickname + ", "
        age.text = items[position].age
        location.text = items[position].location



        return convertView
    }

}