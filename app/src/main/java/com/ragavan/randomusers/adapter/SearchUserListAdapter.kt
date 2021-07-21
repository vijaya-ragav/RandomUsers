package com.ragavan.randomusers.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ragavan.randomusers.R
import com.ragavan.randomusers.model.Results
import javax.inject.Inject

class SearchUserListAdapter @Inject constructor(private val searchResult: List<Results>) : RecyclerView.Adapter<SearchUserListAdapter.MyViewHolder>() {

    var onItemClick: ((Results) -> Unit)? = null

    class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

        val userImageView: ImageView = itemView.findViewById(R.id.user_image_view)
        val userFirstName: TextView = itemView.findViewById(R.id.user_first_name)
        val userLastName: TextView = itemView.findViewById(R.id.user_last_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
       return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_random_user_list,parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = searchResult[position]
        holder.userFirstName.text = item.name.first
        holder.userLastName.text = item.name.last

        Glide.with(holder.itemView.context)
            .load(item.picture.thumbnail)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(holder.userImageView)

        holder.itemView.apply { setOnClickListener { onItemClick?.invoke(searchResult[position]) } }

    }

    override fun getItemCount(): Int {
        return searchResult.size
    }
}