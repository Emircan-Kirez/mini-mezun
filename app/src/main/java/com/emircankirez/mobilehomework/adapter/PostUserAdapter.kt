package com.emircankirez.mobilehomework.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emircankirez.mobilehomework.R
import com.emircankirez.mobilehomework.databinding.PostItemForUserBinding
import com.emircankirez.mobilehomework.model.Post
import com.emircankirez.mobilehomework.model.User
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class PostUserAdapter(val postList : ArrayList<Post>) : RecyclerView.Adapter<PostUserAdapter.PostUserHolder>() {
    private var database = Firebase.database.reference
    private var storage = Firebase.storage

    class PostUserHolder(val binding : PostItemForUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostUserHolder {
        val binding = PostItemForUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostUserHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostUserHolder, position: Int) {
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

            btnDelete.setOnClickListener {
                // gönderi silmek için alertDialog
                val deleteDialog = AlertDialog.Builder(holder.itemView.context)
                deleteDialog.setTitle("Gönderi Sil")
                deleteDialog.setMessage("Gönderiyi silmek istiyor musunuz?")
                deleteDialog.setIcon(R.drawable.baseline_delete_24)
                deleteDialog.setPositiveButton("Sil"){ _, _ ->
                    database.child("posts").child(post.postId).get()
                        .addOnSuccessListener { snapshot ->
                            val post = snapshot.getValue(Post::class.java)!!
                            val photoUrl = post.photoUrl

                            storage.reference.child("postImg").child(photoUrl).delete() // post'un resmini sil
                            database.child("posts").child(post.postId).setValue(null)  // post'u sil
                                .addOnSuccessListener {
                                    Toast.makeText(holder.itemView.context, "Gönderi başarıyla silindi", Toast.LENGTH_SHORT).show()
                                }
                        }
                }
                deleteDialog.setNegativeButton("Kapat"){ dialog, _ ->
                    dialog.dismiss()
                }
                deleteDialog.show()
            }
        }

    }


}