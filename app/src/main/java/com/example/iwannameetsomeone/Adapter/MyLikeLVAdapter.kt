package com.example.iwannameetsomeone.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.TextView
import com.example.iwannameetsomeone.R
import com.example.iwannameetsomeone.auth.UserDataModel
import java.time.LocalDate
import java.util.*

class MyLikeLVAdapter(val context: Context, val items: MutableList<UserDataModel>) : BaseAdapter() {
    // 아이템 총 개수 반환
    override fun getCount(): Int = items.size


    // 아이템 반환
    override fun getItem(position: Int): Any = items[position]


    // 아이템의 아이디 반환
    override fun getItemId(position: Int): Long = position.toLong()

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var convertView = convertView
        if (convertView == null) {

            convertView =
                LayoutInflater.from(parent?.context).inflate(R.layout.my_like_item, parent, false)

        }

        val nickname = convertView!!.findViewById<TextView>(R.id.listViewItemNickname)
        val location = convertView!!.findViewById<TextView>(R.id.listViewItemLocation)
        val age = convertView!!.findViewById<TextView>(R.id.listViewItemAge)



        nickname.text = items[position].nickname + ", "
        age.text =  items[position].age
        location.text = items[position].location



        return convertView!!
    }

}