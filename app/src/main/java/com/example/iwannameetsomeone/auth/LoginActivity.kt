package com.example.iwannameetsomeone.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.iwannameetsomeone.MainActivity
import com.example.iwannameetsomeone.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


// (전역변수) 바인딩 객체 선언
private var vBinding: ActivityLoginBinding? = null

// 매번 null 확인 귀찮음 -> 바인딩 변수 재선언
private val binding get() = vBinding!!

//AUTH
private lateinit var auth: FirebaseAuth

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 자동 생성된 뷰바인딩 클래스에서의 inflate 메서드 활용
        // -> 액티비티에서 사용할 바인딩 클래스의 인스턴스 생성
        vBinding = ActivityLoginBinding.inflate(layoutInflater)
        // -> 생성된 뷰를 액티비티에 표시
        setContentView(binding.root)

        auth = Firebase.auth


        //로그인 버튼
        binding.loginBtn.setOnClickListener {

            //만약 emailEdt가 비어있다면
            if (binding.emailEdt.text?.isEmpty()!!) {
                Toast.makeText(this, "email을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                //아이디,패스워드 입력란
                auth.signInWithEmailAndPassword(
                    binding.emailEdt.text.toString(),
                    binding.passwordEdt.text.toString()
                )
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {//로그인 성공시
                            startActivity(Intent(this, MainActivity::class.java))
                            Toast.makeText(this, "로그인 성공", Toast.LENGTH_LONG).show()

                        } else {//이메일이나 비밀번호가 틀렸다면
                            Toast.makeText(this, "Email 혹은 비밀번호를 확인해주세요", Toast.LENGTH_LONG).show()
                        }
                    }

            }
        }

        //회원가입 액티비티로
        binding.signupBtn.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))

        }
    }

    // 액티비티 파괴시
    override fun onDestroy() {

        // 바인딩 클래스 인스턴스 참조를 정리 -> 메모리 효율이 좋아짐
        vBinding = null
        super.onDestroy()

    }
}