package com.example.iwannameetsomeone.slider

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.iwannameetsomeone.R
import com.example.iwannameetsomeone.auth.UserDataModel
import com.example.iwannameetsomeone.settings.listviewAdapter

import com.example.iwannameetsomeone.utils.FirebaseAuthUtils
import com.example.iwannameetsomeone.utils.FirebaseRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*

private val TAG = "CardStackAdapter"
private val likeUserListUid = mutableListOf<String>()
private val likeUserList = mutableListOf<UserDataModel>()
private val uid = FirebaseAuthUtils.getUid()

class CardStackAdapter(val context: Context, val items: List<UserDataModel>) :
    RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardStackAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.item_card, parent, false)
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
        val age = itemView.findViewById<TextView>(R.id.itemAge)
        val location = itemView.findViewById<TextView>(R.id.itemLocation)


        fun binding(data: UserDataModel) {


                val storageRef = Firebase.storage.reference.child(data.uid + ".png")
                storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->

                    if (task.isSuccessful) {
                        Glide.with(context)
                            .load(task.result)
                            .into(image)

                    }

                })
                nickname.text =data.nickname
                age.text = ", " + data.age
                location.text =  data.location
            }
        }
    }
