package com.example.iwannameetsomeone.slider

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.iwannameetsomeone.R
import com.example.iwannameetsomeone.auth.UserDataModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*

class CardStackAdapter(val context: Context, val items: List<UserDataModel>) :
    RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardStackAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view : View = inflater.inflate(R.layout.item_card, parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardStackAdapter.ViewHolder, position: Int) {
        holder.binding(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val image = itemView.findViewById<ImageView>(R.id.profileImageArea)
        val nickname = itemView.findViewById<TextView>(R.id.itemNickname)
        val birth = itemView.findViewById<TextView>(R.id.itemBirth)
        val location = itemView.findViewById<TextView>(R.id.itemLocation)


        fun binding(data: UserDataModel) {

            val storageRef = Firebase.storage.reference.child(data.uid + ".png")
            storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->

                if(task.isSuccessful) {
                    Glide.with(context)
                        .load(task.result)
                        .into(image)

                }

            })
            nickname.text = "닉네임: " + data.nickname
            birth.text = "생년월일: " + data.birth
            location.text = "지역: " + data.location
        }
    }
    fun calculateAge(date: Date?): Int {
        val birthCalendar = Calendar.getInstance()
        birthCalendar.time = date ?: Date()
        val current = Calendar.getInstance()
        val currentYear = current[Calendar.YEAR]
        val currentMonth = current[Calendar.MONTH]
        val currentDay = current[Calendar.DAY_OF_MONTH]
        var age = currentYear - birthCalendar[Calendar.YEAR]
        if (birthCalendar[Calendar.MONTH] * 100 +
            birthCalendar[Calendar.DAY_OF_MONTH] > currentMonth * 100 + currentDay
        ) age--
        return age
    }
}