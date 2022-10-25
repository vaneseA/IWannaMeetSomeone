package com.example.iwannameetsomeone.Adapter

import android.content.Context
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

class ListViewAdapter(val context : Context, val items : MutableList<UserDataModel>) : BaseAdapter(){
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        if (convertView == null) {

            convertView = LayoutInflater.from(parent?.context).inflate(R.layout.list_view_item, parent, false)

        }

        val nickname = convertView!!.findViewById<TextView>(R.id.listViewItemNickname)
        val location = convertView!!.findViewById<TextView>(R.id.listViewItemLocation)
        val age = convertView!!.findViewById<TextView>(R.id.listViewItemAge)



        nickname.text = items[position].nickname
        age.text = items[position].age
        location.text = items[position].location



        return convertView!!
    }

}