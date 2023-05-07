package com.emircankirez.mobilehomework.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.emircankirez.mobilehomework.model.Announcement
import com.emircankirez.mobilehomework.activities.AnnouncementDetailsActivity
import com.emircankirez.mobilehomework.databinding.AnnouncementItemBinding

class AnnouncementAdapter (private val announcementList : ArrayList<Announcement>) : RecyclerView.Adapter<AnnouncementAdapter.AnnouncementHolder>(){

    class AnnouncementHolder(val binding : AnnouncementItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementHolder {
        val binding = AnnouncementItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnnouncementHolder(binding)
    }

    override fun getItemCount(): Int {
        return announcementList.size
    }

    override fun onBindViewHolder(holder: AnnouncementHolder, position: Int) {
        holder.binding.txtTitle.text = announcementList[position].title
        holder.binding.txtDateInfo.text = "Son Tarih: " + announcementList[position].lastDate

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, AnnouncementDetailsActivity::class.java)
            intent.putExtra("detail", announcementList[position]) // hangi duyuruya tıklandıysa onu diğer activity'e aktar
            holder.itemView.context.startActivity(intent)
        }
    }

}