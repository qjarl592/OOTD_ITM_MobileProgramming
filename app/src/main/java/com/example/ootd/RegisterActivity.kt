package com.example.ootd

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.ootd.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.registerEndBtn.setOnClickListener{
            val new_email = binding.registerEmail.text.toString()
            val new_password = binding.registerPassword.text.toString()
            Log.d("log", "id : ${new_email}, pw : ${new_password}")

            Register(new_email,new_password)

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    fun Register(email:String, password:String){
        Log.d("log", "id : ${email}, pw : ${password}")
        auth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener{
                task ->
            if(task.isSuccessful){
                Log.d("log", "성공")
            }else{
                Log.d("log", "실패")
            }
        }
    }
}