package com.emircankirez.mobilehomework.bottomnavfragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.emircankirez.mobilehomework.activities.AddAnnouncementActivity
import com.emircankirez.mobilehomework.adapter.AnnouncementAdapter
import com.emircankirez.mobilehomework.databinding.FragmentHomeBinding
import com.emircankirez.mobilehomework.model.Announcement
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AnnouncementFragment : Fragment() {
    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var database : DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = Firebase.database.reference

        binding.apply {
            recyclerViewAnnouncement.layoutManager = LinearLayoutManager(requireContext())
            fabAddAnnouncement.setOnClickListener {
                val intent = Intent(requireContext(), AddAnnouncementActivity::class.java)
                startActivity(intent)
            }
        }


        // biz duyuru ekranındayken başkası duyuru eklerse
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressBar.visibility = View.VISIBLE
                val format = SimpleDateFormat("dd/MM/yyyyy")
                val list = ArrayList<Announcement>()
                val currentDay = format.format(Calendar.getInstance().time)
                val currentDate = format.parse(currentDay)!!
                for(ds in snapshot.children.reversed()){
                    val announcement = ds.getValue(Announcement::class.java)!!
                    val announcementDate = format.parse(announcement.lastDate)!!
                    if(announcementDate >= currentDate){ // tarihi geçmemiş olan duyuruları ekle
                        list.add(announcement)
                    }
                }
                val adapter = AnnouncementAdapter(list)
                binding.recyclerViewAnnouncement.adapter = adapter
                binding.progressBar.visibility = View.INVISIBLE
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        database.child("announcements").addValueEventListener(valueEventListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}