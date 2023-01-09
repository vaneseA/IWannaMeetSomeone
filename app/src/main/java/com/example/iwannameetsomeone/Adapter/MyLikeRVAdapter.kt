package com.example.iwannameetsomeone.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.iwannameetsomeone.R
import com.example.iwannameetsomeone.auth.UserDataModel

class MyLikeRVAdapter(val context: Context, val dataSet: List<UserDataModel>) :
    RecyclerView.Adapter<MyLikeRVAdapter.ViewHolder>() {

    interface ItemClick {
        fun onClick(view: View, position: Int)
    }

    var itemClick: ItemClick? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val nickname = view.findViewById<TextView>(R.id.listViewItemNickname)
        val location = view.findViewById<TextView>(R.id.listViewItemLocation)
        val age = view.findViewById<TextView>(R.id.listViewItemAge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.my_like_item, parent, false)

        return ViewHolder(view)

    }


    override fun onBindViewHolder(holder: MyLikeRVAdapter.ViewHolder, position: Int) {

        holder.itemView.setOnClickListener { v ->
            itemClick?.onClick(v, position)
        }

        holder.nickname.text = dataSet[position].nickname + ", "
        holder.age.text = dataSet[position].age
        holder.location.text = dataSet[position].location
//        val selected = dataSet[position].selected
//        if (selected) {
//            holder.likeBtn.setImageResource(R.drawable.like_red)
//        } else {
//            holder.likeBtn.setImageResource(R.drawable.like_grey)
//        }


    }
    override fun getItemCount(): Int {
        return dataSet.size
    }



}