  package com.example.iwannameetsomeone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.example.iwannameetsomeone.auth.LoginActivity
import com.example.iwannameetsomeone.slider.CardStackAdapter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import kotlinx.android.synthetic.main.activity_main.*

  class MainActivity : AppCompatActivity() {

    lateinit var cardStackAdapter: CardStackAdapter
    lateinit var manager : CardStackLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LogoutBtn2.setOnClickListener {

            val auth = Firebase.auth
            auth.signOut()

            startActivity(Intent(this,LoginActivity::class.java))
        }


        val cardStackView = findViewById<CardStackView>(R.id.cardStackView)

        manager = CardStackLayoutManager(baseContext,object : CardStackListener{
            override fun onCardDragging(direction: Direction?, ratio: Float) {

            }

            override fun onCardSwiped(direction: Direction?) {

            }

            override fun onCardRewound() {

            }

            override fun onCardCanceled() {

            }

            override fun onCardAppeared(view: View?, position: Int) {

            }

            override fun onCardDisappeared(view: View?, position: Int) {

            }

        })
        val  tesList = mutableListOf<String>()
        tesList.add("a")
        tesList.add("b")
        tesList.add("c")


        cardStackAdapter = CardStackAdapter(baseContext, tesList)
        cardStackView.layoutManager = manager
        cardStackView.adapter = cardStackAdapter
    }
}