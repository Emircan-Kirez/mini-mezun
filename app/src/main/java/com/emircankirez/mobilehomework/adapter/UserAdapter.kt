package com.emircankirez.mobilehomework.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.emircankirez.mobilehomework.activities.UserInfoActivity
import com.emircankirez.mobilehomework.databinding.UserItemBinding
import com.emircankirez.mobilehomework.model.User

class UserAdapter(val users : ArrayList<User>) : RecyclerView.Adapter<UserAdapter.UserHolder>() {
    class UserHolder(val binding : UserItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val binding = UserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserHolder(binding)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        holder.binding.apply {
            val user = users[position]
            txtFullName.text = "${user.name} ${user.surname}"
            txtEntryYear.text = "Giriş Yılı: ${user.entryYear}"
            txtGraduationYear.text = "Mezun Yılı: ${user.graduationYear}"
            txtCity.text = "Şehir: ${user.city}"
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, UserInfoActivity::class.java)
            intent.putExtra("user", users[position])
            holder.itemView.context.startActivity(intent)
        }
    }
}