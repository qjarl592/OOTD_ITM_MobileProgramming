package com.example.ootd

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ootd.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val new_email = binding.registerEmail.text.toString()
        val new_password = binding.registerPassword.text.toString()

        binding.registerEndBtn.setOnClickListener{
            Register(new_email,new_password)

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    fun Register(email:String, password:String){
        auth?.createUserWithEmailAndPassword(email, password)
    }
}