package com.emircankirez.mobilehomework.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

import androidx.fragment.app.Fragment
import com.emircankirez.mobilehomework.R
import com.emircankirez.mobilehomework.bottomnavfragments.AnnouncementFragment
import com.emircankirez.mobilehomework.bottomnavfragments.ProfileFragment
import com.emircankirez.mobilehomework.bottomnavfragments.GraduatedFragment
import com.emircankirez.mobilehomework.bottomnavfragments.PostFragment
import com.emircankirez.mobilehomework.databinding.ActivityMainBinding
import com.emircankirez.mobilehomework.login.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        database = Firebase.database.reference

        sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)

        val intent = intent
        val info = intent.getStringExtra("info")

        if(info != null){ // eğer info varsa post eklerken geri dönmüşüzdür
            replaceFragment(PostFragment())
            binding.bottomNavView.selectedItemId = R.id.post
        }else{ // ilk defa açılıyordur ya da duyuru eklerken geri dönmüşüzdür
            replaceFragment(AnnouncementFragment())
            binding.bottomNavView.selectedItemId = R.id.announcement
        }

        binding.apply {

            bottomNavView.setOnItemSelectedListener {
                when(it.itemId){
                    R.id.announcement -> {
                        replaceFragment(AnnouncementFragment())
                        true
                    }
                    R.id.profile -> {
                        replaceFragment(ProfileFragment())
                        true
                    }
                    R.id.graduated -> {
                        replaceFragment(GraduatedFragment())
                        true
                    }
                    R.id.post -> {
                        replaceFragment(PostFragment())
                        true
                    }
                    else -> false
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.sign_out, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.sign_out){
            // sign out
            auth.signOut()
            val intent = Intent(this@MainActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment).commit()
    }


}