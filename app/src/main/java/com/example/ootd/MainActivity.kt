package com.example.ootd

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ootd.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    var storage : FirebaseStorage? = null
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    var outfitSDTOList: ArrayList<Pair<String, outfitsDTO>> = arrayListOf()
    var OOTD_Adapter: OOTDAdapter = OOTDAdapter(outfitSDTOList)
    var filter: Boolean = true
    var timeInterval: Long = 7*24*60*60*1000
    var weekInterval: Long = 1
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initialize required tools.
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //A function for loading all images stored in firestore.
        getAll()

        //Binding the adapter and recyclerview.
        binding.recyclerView.adapter = OOTD_Adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        //When "All" button is clicked, shows the all outfit posts.
        binding.allBtn.setOnClickListener{
            if(!filter) {
                binding.uploadBtn.visibility = View.VISIBLE
                binding.loadMoreBtn.visibility = View.VISIBLE
                getAll()
                filter = true
            }
        }

        //When "All" button is clicked, shows the all outfit posts.
        binding.mineBtn.setOnClickListener{
            if(filter) {
                binding.uploadBtn.visibility = View.INVISIBLE
                binding.loadMoreBtn.visibility = View.INVISIBLE
                getMine()
                filter = false
            }
        }

        //When "All" button is clicked, shows the all outfit posts.
        binding.logoutBtn.setOnClickListener{
            val goLogin = Intent(this, LoginActivity::class.java)
            startActivity(goLogin)
        }

        //When "Mine" button is clicked, shows posts only the user posted.
        binding.uploadBtn.setOnClickListener{
            val selectPhotoIntent = Intent(Intent.ACTION_GET_CONTENT)
            selectPhotoIntent.type = "image/*"
            startForResult.launch(selectPhotoIntent)
        }

        //When "Load more" button is clicked, shows more posts that are uploaded 1 more weeks ago.
        binding.loadMoreBtn.setOnClickListener {
            if(filter) {
                weekInterval += 1
                getAll()
            }
        }

    }

    //A function for loading all image files url stored in firestore.
    //It shows recently uploaded posts at the top.
    fun getAll() {
        firestore?.collection("images")?.whereGreaterThan("timestamp", System.currentTimeMillis()-timeInterval*weekInterval)?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            outfitSDTOList.clear()
            for(snapshot in querySnapshot!!.documents){
                val docId = snapshot.id
                val item = snapshot.data
                val newOutfitDTO = outfitsDTO(item?.get("imageURI").toString(), item?.get("uid").toString(), item?.get("userId").toString(), item?.get("timestamp").toString().toLong())
                outfitSDTOList.add(Pair(docId, newOutfitDTO))
            }
            outfitSDTOList.reverse()
            OOTD_Adapter.notifyDataSetChanged()
        }
    }

    //A function for loading only my image files url stored in firestore.
    //It also shows recently uploaded posts at the top.
    fun getMine() {
        firestore?.collection("images")?.whereEqualTo("userId", auth?.currentUser?.email)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            outfitSDTOList.clear()
            for(snapshot in querySnapshot!!.documents){
                val docId = snapshot.id
                val item = snapshot.data
                val newOutfitDTO = outfitsDTO(item?.get("imageURI").toString(), item?.get("uid").toString(), item?.get("userId").toString(), item?.get("timestamp").toString().toLong())
                outfitSDTOList.add(Pair(docId, newOutfitDTO))
            }
            outfitSDTOList.sortBy { it.second.timestamp }
            outfitSDTOList.reverse()
            OOTD_Adapter.notifyDataSetChanged()
        }
    }

    //Loading image files stored in android gallery.
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        //If an image is selected, upload it at the firestore and firebase storage.
        if(result.resultCode == RESULT_OK){
            val intent: Intent = result.data!!
            val photoUri = intent.data!!
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                upload(photoUri)
            }
        }
        //If an image is not selected, finish this process.
        else{
            finish()
        }
    }

    //A function for uploading file into firestore and firebase storage.
    fun upload(photoUri: Uri) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMAGE_" + timestamp + "_.png"

        val storageRef = storage?.reference?.child("images/$imageFileName")
        storageRef?.putFile(photoUri!!)
            ?.addOnFailureListener{ e ->
                Log.d("log","f${e.message}")
            }?.addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener {
                    var outfitsDTO = outfitsDTO(
                        it.toString(),
                        auth?.currentUser?.uid,
                        auth?.currentUser?.email,
                        System.currentTimeMillis()
                    )
                    firestore?.collection("images")?.document()?.set(outfitsDTO)?.addOnSuccessListener {
                        Toast.makeText(this, "success", Toast.LENGTH_LONG).show()
                    }?.addOnFailureListener { err->
                        Log.d("log","${err}")
                    }
                }
            }
        OOTD_Adapter.notifyDataSetChanged()
    }
}

