package com.example.ootd

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ootd.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    //Read firebase authentication library
    var auth : FirebaseAuth? = null
    //Google login class
    var googleSignInClinet : GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        //When login button clicked, process login with the data user input in the edit text box.
        binding.loginBtn.setOnClickListener{
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            login(email, password)
        }

        //When google login button clicked, process social login by using google OAuth.
        binding.googleLoginBtn.setOnClickListener{
            googleLogin()
        }

        //When register button clicked, move to RegiterActivity that contains register form.
        binding.registerBtn.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            //Read google API key
            .requestIdToken(getString(R.string.default_web_client_id))
            //Read email
            .requestEmail()
            .build()
        googleSignInClinet = GoogleSignIn.getClient(this,gso)
    }

    //A function for Google login
    fun googleLogin(){
        val intent = googleSignInClinet?.signInIntent
        startForResult.launch(intent)
    }

    //A function for Google login contains specific logic.
    //By using google id, register for our app first. Then, try login to our app.
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result: ActivityResult ->
        if(result.resultCode == RESULT_OK){
            val intent: Intent = result.data!!
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(intent)
            try{
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            }
            catch(e: ApiException){
                Log.w(ContentValues.TAG, "google login failed")
            }
        }
    }

    //A function for google login by using token.
    fun firebaseAuthWithGoogle(account:GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener{
                    task ->
                if(task.isSuccessful){
                    //If login proceed successfully, go to main activity.
                    goMain(task.result?.user)
                }else{
                    //If login failed, toast a error message.
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    //A function for firebase login.
    fun login(email:String, password:String){
        auth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener{
                    task ->
                if(task.isSuccessful){
                    //If login proceed successfully, go to main activity.
                    goMain(task.result?.user)
                }else{
                    //If login failed, toast a error message.
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    //A function for a movement to main activity.
    fun goMain(user: FirebaseUser?){
        if(user != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}