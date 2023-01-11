package com.example.iwannameetsomeone.Message.fcm

import com.example.iwannameetsomeone.Message.fcm.Repo.Companion.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 레트로핏 인스턴스
class RetrofitInstance {

    companion object {

        // (레트로핏) 객체 지연초기화
        private val retrofit by lazy {

            // 빌더
            Retrofit.Builder()

                // 통신할 서버 URL
                .baseUrl(BASE_URL)

                // 서버로부터 받아온 데이터(컨텐츠)를 원하는 타입으로 바꿈
                .addConverterFactory(GsonConverterFactory.create())

                // 빌드
                .build()

        }

        // (레트로핏) 객체 생성
        val api = retrofit.create(NotiAPI::class.java)

    }


}