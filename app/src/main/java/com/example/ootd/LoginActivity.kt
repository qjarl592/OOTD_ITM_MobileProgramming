package com.example.ootd

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.ootd.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    //인증 라이브러리 불러옴
    var auth : FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(vinding.root)
        auth = FirebaseAuth.getInstance()

        vinding.imageView.setImageResource(R.drawable.logo)

        vinding.loginBtn.setOnClickListener{
            val email = vinding.emailInput.text.toString()
            val password = vinding.passwordInput.text.toString()
            loginAndRegister(email, password)
        }
    }
    fun loginAndRegister(email:String, password:String){
        auth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener{
            task ->
                if(task.isSuccessful){
//                    회원가입 성공시
                    goMain(task.result?.user)
                }else if(!task.exception?.message.isNullOrEmpty()){
//                    회원가입 실패시 에러메세지 출력
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                }else{
//                    로그인
                    login(email, password)
                }
        }
    }
    fun login(email:String, password:String){
        auth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener{
                    task ->
                if(task.isSuccessful){
//                    로그인 성공시
                    goMain(task.result?.user)
                }else{
//                    로그인 실패시
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }
    fun goMain(user: FirebaseUser?){
        if(user != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}