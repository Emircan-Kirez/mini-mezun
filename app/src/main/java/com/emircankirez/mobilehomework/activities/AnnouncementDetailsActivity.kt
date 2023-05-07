package com.emircankirez.mobilehomework.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.emircankirez.mobilehomework.R
import com.emircankirez.mobilehomework.databinding.ActivityDetailsBinding
import com.emircankirez.mobilehomework.model.Announcement
import com.emircankirez.mobilehomework.model.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class AnnouncementDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private lateinit var database : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = Firebase.database.reference

        val intent = intent
        val announcement  = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("detail", Announcement::class.java)
        } else {
            intent.getParcelableExtra("detail") as Announcement?
        }


        binding.apply {

            if(announcement != null) {
                Glide.with(this@AnnouncementDetailsActivity).load(announcement.imageUrl).into(imgAnnouncement)
                txtAnnouncementTitle.text = announcement.title
                txtAnnouncementContent.text = "        ${announcement.content}"
                txtAnnouncementDate.text = "Son Tarih: ${announcement.lastDate}"

                database.child("users").child(announcement.userId).get()
                    .addOnSuccessListener { snapshot ->
                        val user = snapshot.getValue<User>()!!
                        txtAnnouncementWriter.text = "Yazar: ${user?.name} ${user?.surname}"

                        btnUserInfo.setOnClickListener {
                            val intent = Intent(this@AnnouncementDetailsActivity, UserInfoActivity::class.java)
                            intent.putExtra("user", user)
                            startActivity(intent)
                        }
                    }

            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.btn_back, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.btnBack){
            val intent = Intent(this@AnnouncementDetailsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}