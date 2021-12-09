package com.example.ootd

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ootd.databinding.ActivityMainBinding
import com.example.ootd.databinding.ItemViewBinding
import com.example.ootd.outfitsDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.time.LocalDateTime
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

        //init
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        getAll()
        binding.recyclerView.adapter = OOTD_Adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
//        Log.d("log","${outfitSDTOList}")
//        OOTD_Adapter = OOTDAdapter(outfitSDTOList)
//        binding.recyclerView.adapter = OOTD_Adapter
//        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.allBtn.setOnClickListener{
            Log.d("log","all is clicked!")
            if(!filter) {
                binding.uploadBtn.visibility = View.VISIBLE
                binding.loadMoreBtn.visibility = View.VISIBLE
                getAll()
                filter = true
            }
        }

        binding.mineBtn.setOnClickListener{
            Log.d("log","mineBtn is clicked!")
            if(filter) {
                binding.uploadBtn.visibility = View.INVISIBLE
                binding.loadMoreBtn.visibility = View.INVISIBLE
                getMine()
                filter = false
            }
        }

        binding.logoutBtn.setOnClickListener{
            val goLogin = Intent(this, LoginActivity::class.java)
            startActivity(goLogin)
            Log.d("log","logoutBtn is clicked!")
        }

        binding.uploadBtn.setOnClickListener{
            Log.d("log","uploadBtn is clicked!")
            val selectPhotoIntent = Intent(Intent.ACTION_GET_CONTENT)
            selectPhotoIntent.type = "image/*"
            startForResult.launch(selectPhotoIntent)
            //Log.d("log", "${ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)}")
//            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
//                upload()
//                Log.d("log","uploadBtn is clicked!")
//            }
        }

        binding.loadMoreBtn.setOnClickListener {
            Log.d("log","load more is clicked!")
            if(filter) {
                weekInterval += 1
                getAll()
            }
        }

    }
    fun getAll() {
        firestore?.collection("images")?.whereGreaterThan("timestamp", System.currentTimeMillis()-timeInterval*weekInterval)?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            outfitSDTOList.clear()
            for(snapshot in querySnapshot!!.documents){
                val docId = snapshot.id
                val item = snapshot.data
                val newOutfitDTO = outfitsDTO(item?.get("imageURI").toString(), item?.get("uid").toString(), item?.get("userId").toString(), item?.get("timestamp").toString().toLong())
                Log.d("log", "document ID = ${docId}")
                Log.d("log","item = ${newOutfitDTO}")
                outfitSDTOList.add(Pair(docId, newOutfitDTO))
            }
            outfitSDTOList.reverse()
            OOTD_Adapter.notifyDataSetChanged()
        }
    }

    fun getMine() {
        firestore?.collection("images")?.whereEqualTo("userId", auth?.currentUser?.email)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            outfitSDTOList.clear()
            for(snapshot in querySnapshot!!.documents){
                val docId = snapshot.id
                val item = snapshot.data
                val newOutfitDTO = outfitsDTO(item?.get("imageURI").toString(), item?.get("uid").toString(), item?.get("userId").toString(), item?.get("timestamp").toString().toLong())
                Log.d("log", "document ID = ${docId}")
                Log.d("log","item = ${newOutfitDTO}")
                outfitSDTOList.add(Pair(docId, newOutfitDTO))
            }
            outfitSDTOList.sortBy { it.second.timestamp }
            outfitSDTOList.reverse()
            OOTD_Adapter.notifyDataSetChanged()
        }
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        //사진선택
        if(result.resultCode == RESULT_OK){
            val intent: Intent = result.data!!
            val photoUri = intent.data!!
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Log.d("log","upload is called")
                upload(photoUri)
            }
        }
        //사진선택X
        else{
            finish()
        }
    }

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
                    Log.d("log","${outfitsDTO}")
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

