package com.example.ootd

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.ootd.databinding.ItemViewBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*

//Adapter for recycler view
class OOTDAdapter(val myList: MutableList<Pair<String, outfitsDTO>>): RecyclerView.Adapter<OOTDAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item_number = myList.get(position)
        holder.bind(item_number)
    }

    override fun getItemCount(): Int {
        return myList.size
    }

    inner class ViewHolder(val binding: ItemViewBinding, val context: Context): RecyclerView.ViewHolder(binding.root){
        private val auth: FirebaseAuth? = FirebaseAuth.getInstance()
        private val firestore: FirebaseFirestore? = FirebaseFirestore.getInstance()
        private val storage: FirebaseStorage? = FirebaseStorage.getInstance()

        fun bind(OOTD:Pair<String, outfitsDTO>) {
            if(auth?.currentUser?.email != OOTD.second.userId) binding.imageView2.visibility = View.INVISIBLE
            binding.imageView2.setOnClickListener {
                Log.d("log", "???")
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Inform")
                builder.setMessage("Do you want to delete this post")
                builder.setNegativeButton("No"){dialogInterface:DialogInterface, i:Int ->
                    Log.d("log", "no!!")
                }
                builder.setPositiveButton("YES"){dialogInterface:DialogInterface, i:Int ->
                    Log.d("log", "yes!!")
                    val docId = OOTD.first
                    val imagePath = OOTD.second.imageURI
                    firestore?.collection("images")?.document(docId.toString())?.delete()?.addOnSuccessListener {
                        val destRef = storage?.getReferenceFromUrl(imagePath.toString())
                        destRef?.delete()?.addOnSuccessListener {
                            Log.d("log", "성공!!!")
                            Toast.makeText(context, "deleting success", Toast.LENGTH_LONG).show()
                        }?.addOnFailureListener { e->
                            Log.d("log", "error : ${e}")
                        }
                    }
                }
                builder.create()
                builder.show()
            }
            Glide.with(binding.outfitImg.context).load(OOTD.second.imageURI).into(binding.outfitImg)
            val date = SimpleDateFormat("yyyy-MM-dd kk:mm:ss E", Locale("ko", "KR")).format(OOTD.second.timestamp?.let { Date(it) })
            binding.userId.text = OOTD.second.userId
            binding.timeStamp.text = " | "+ date
        }
    }
}