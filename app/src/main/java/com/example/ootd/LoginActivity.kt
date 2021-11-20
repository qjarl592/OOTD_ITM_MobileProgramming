package com.example.ootd

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ootd.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.Auth
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
    //인증 라이브러리 불러옴
    var auth : FirebaseAuth? = null
//    구글 로그인 클래스
    var googleSignInClinet : GoogleSignInClient? = null

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
        vinding.googleLoginBtn.setOnClickListener{
            googleLogin()
        }

        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//          구글 API 키 받아오기 1058798317703-hkt5or47rorvsuc7ktndl16ei42vdgve.apps.googleusercontent.com
            .requestIdToken(getString(R.string.default_web_client_id))
//          이메일 받아오기
            .requestEmail()
            .build()
        googleSignInClinet = GoogleSignIn.getClient(this,gso)
    }

    fun googleLogin(){
        val intent = googleSignInClinet?.signInIntent
        startForResult.launch(intent)
    }
// https://korean-otter.tistory.com/entry/startActivityForResult-deprecated-in-kotlin-firebase-google-login 참고
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

    fun firebaseAuthWithGoogle(account:GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)
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