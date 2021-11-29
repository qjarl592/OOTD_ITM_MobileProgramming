package com.example.ootd

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
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
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class MainActivity : AppCompatActivity() {
    var storage : FirebaseStorage? = null
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
  //  lateinit var OOTD_Adapter: OOTDAdapter
    var outfitSDTOList: ArrayList<outfitsDTO> = arrayListOf()
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            for(snapshot in querySnapshot!!.documents){
                val item = snapshot.data
                val newOutfitDTO = outfitsDTO(item?.get("imageURI").toString(), item?.get("uid").toString(), item?.get("userId").toString(), item?.get("timestamp").toString())
                Log.d("log","item = ${newOutfitDTO}")
                outfitSDTOList.add(newOutfitDTO)
            }
            outfitSDTOList.reverse()
            val OOTD_Adapter = OOTDAdapter(outfitSDTOList)
            binding.recyclerView.adapter = OOTD_Adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
        }
//        Log.d("log","${outfitSDTOList}")
//        OOTD_Adapter = OOTDAdapter(outfitSDTOList)
//        binding.recyclerView.adapter = OOTD_Adapter
//        binding.recyclerView.layoutManager = LinearLayoutManager(this)


        ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)

        binding.allBtn.setOnClickListener{
            Log.d("log","all is clicked!")
        }
        binding.mineBtn.setOnClickListener{
            Log.d("log","mineBtn is clicked!")
        }
        binding.logoutBtn.setOnClickListener{
            val goLogin = Intent(this, LoginActivity::class.java)
            startActivity(goLogin)
            Log.d("log","logoutBtn is clicked!")
        }
        binding.uploadBtn.setOnClickListener{
            //ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
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
    fun upload(photoUri: Uri){
        //binding.imageView3.setImageURI(photoUri)

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMAGE_" + timestamp + "_.png"

        val storageRef = storage?.reference?.child("images/$imageFileName")
        val uploadTask = storageRef?.putFile(photoUri!!)
            ?.addOnFailureListener{ e ->
                Log.d("log","f${e.message}")
            }?.addOnSuccessListener {
                Toast.makeText(this, "success", Toast.LENGTH_LONG).show()
                storageRef.downloadUrl.addOnSuccessListener {
                    var outfitsDTO = outfitsDTO(
                        it.toString(),
                        auth?.currentUser?.uid,
                        auth?.currentUser?.email,
                        System.currentTimeMillis().toString()
                        )
                    Log.d("log","${outfitsDTO}")
                    firestore?.collection("images")?.document()?.set(outfitsDTO)?.addOnFailureListener { err->
                        Log.d("log","${err}")
                    }
                }
            }
    }
    //Adapter for recycler view
    class OOTDAdapter(val myList: MutableList<outfitsDTO>): RecyclerView.Adapter<OOTDAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val lotto_number = myList.get(position)
            holder.bind(lotto_number)
        }

        override fun getItemCount(): Int {
            return myList.size
        }

        class ViewHolder(val binding: ItemViewBinding): RecyclerView.ViewHolder(binding.root){
            fun bind(OOTD:outfitsDTO) {
                //show lotto numbers
                //Glide.with(binding.outfitImg.context).load(OOTD.imageURI).into(binding.outfitImg)
                binding.outfitImg.setImageResource(R.drawable.logo)
                Log.d("log","userId = ${OOTD.userId}, timesampt = ${OOTD.timestamp}")
                binding.userId.text = OOTD.userId
                binding.timeStamp.text = " | "+OOTD.timestamp
            }
        }
    }
}

