package com.example.iwannameetsomeone.Message.fcm

import com.example.iwannameetsomeone.Message.fcm.Repo.Companion.CONTENT_TYPE
import com.example.iwannameetsomeone.Message.fcm.Repo.Companion.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// 레트로핏 -> 서버와 통신할 때 쓰는 라이브러리

// retrofit 2.0의 장점
// 1. 자체적 비동기 실행과 스레드 관리 가능 -> 속도 향상
// 2. 함수 호출시 파라미터를 넘겨줌 -> 작업량 감소
// 3. 인터페이스 내에서 어노테이션을 사용, 직관적 설계 -> 코드 가독성 향상

interface NotiAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(@Body notification: PushNotification): Response<ResponseBody>

}