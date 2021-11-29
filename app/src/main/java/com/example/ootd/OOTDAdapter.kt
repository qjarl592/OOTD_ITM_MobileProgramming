package com.example.ootd

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ootd.databinding.ItemViewBinding
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

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
            Glide.with(binding.outfitImg.context).load(OOTD.imageURI).into(binding.outfitImg)
//            binding.outfitImg.setImageResource(R.drawable.logo)
            val date = SimpleDateFormat("yyyy-MM-dd kk:mm:ss E", Locale("ko", "KR")).format(
                OOTD.timestamp?.let { Date(it) })
            binding.userId.text = OOTD.userId
            binding.timeStamp.text = " | "+ date

        }
    }
}