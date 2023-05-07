package com.emircankirez.mobilehomework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emircankirez.mobilehomework.databinding.PostItemForEveryoneBinding
import com.emircankirez.mobilehomework.model.Post
import com.emircankirez.mobilehomework.model.User
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class PostEveryoneAdapter(val postList: ArrayList<Post>) : RecyclerView.Adapter<PostEveryoneAdapter.PostEveryoneHolder>() {
    private var database = Firebase.database.reference

    class PostEveryoneHolder(val binding : PostItemForEveryoneBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostEveryoneHolder {
        val binding = PostItemForEveryoneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostEveryoneHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostEveryoneHolder, position: Int) {
        holder.binding.apply {
            val post = postList[position]

            database.child("users").child(post.userId).get()
                .addOnSuccessListener { snapshot ->
                    val user = snapshot.getValue(User::class.java)!!
                    txtPoster.text = "by ${user?.name} ${user?.surname}"
                }

            Glide.with(holder.itemView).load(post.photoUrl).into(imgPost)
            txtPostCaption.text = post.caption
            txtDate.text = "Tarih: " + post.date


        }
    }


}