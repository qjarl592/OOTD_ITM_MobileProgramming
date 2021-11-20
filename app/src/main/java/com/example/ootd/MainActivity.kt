package com.example.ootd

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ootd.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuthSettings
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest
import kotlin.reflect.typeOf

class MainActivity : AppCompatActivity() {
    var PICK_IMAGE_FROK_ALBUM = 0
    var storage : FirebaseStorage? = null
    //var photoUri : Uri?= null
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)

        binding.allBtn.setOnClickListener{
            Log.d("log","all is clicked!")
        }
        binding.mineBtn.setOnClickListener{
            Log.d("log","mineBtn is clicked!")
        }
        binding.logoutBtn.setOnClickListener{
            Log.d("log","logoutBtn is clicked!")
        }
        binding.uploadBtn.setOnClickListener{
            //ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
            Log.d("log","uploadBtn is clicked!")
            val selectPhotoIntent = Intent(Intent.ACTION_PICK)
            selectPhotoIntent.type = "image/*"
            startForResult.launch(selectPhotoIntent)
            //Log.d("log", "${ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)}")
//            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
//                upload()
//                Log.d("log","uploadBtn is clicked!")
//            }
        }
//        Initiate storage
        storage = FirebaseStorage.getInstance()
    }
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        //사진선택
        if(result.resultCode == RESULT_OK){
            val intent: Intent = result.data!!
            val photoUri = intent.data!!
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Log.d("log","got permission")
                upload(photoUri)
            }
        }
        //사진선택X
        else{
            finish()
        }
    }
    fun upload(photoUri: Uri){
        binding.imageView3.setImageURI(photoUri)
//        val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver,photoUri)
//        val baos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//        val photodata = baos.toByteArray()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMAGE_" + timestamp + "_.png"

        val storageRef = storage?.reference?.child("images")?.child(imageFileName)
        var uploadTask = storageRef?.putFile(photoUri!!)
        //var uploadTask = storageRef?.putBytes(photodata)
        uploadTask?.addOnFailureListener{ e->
            Log.d("log","fail sibal ${e.message}")
        }


    }
}