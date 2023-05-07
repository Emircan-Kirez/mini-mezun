package com.emircankirez.mobilehomework.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.emircankirez.mobilehomework.activities.MainActivity
import com.emircankirez.mobilehomework.databinding.ActivitySignInBinding
import com.emircankirez.mobilehomework.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignInBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference
    private lateinit var sharedPreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        database = Firebase.database.reference
        sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)

        binding.apply {
            btnSignIn.setOnClickListener {
                // sign in
                signIn()
            }

            txtNoAccount.setOnClickListener {
                val intent = Intent(this@SignInActivity, SignUpActivity::class.java)
                startActivity(intent)
            }

            txtForgotPassword.setOnClickListener {
                val intent = Intent(this@SignInActivity, ForgotPasswordActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // kayıtlı kullanıcı varsa doğrudan ana ekrana geçsin
        val currentUser = auth.currentUser
        if(currentUser != null){
            reload()
        }
    }

    private fun reload(){
        val intent = Intent(this@SignInActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun signIn(){
        val email = binding.txtEmail.text.toString()
        val password = binding.txtPassword.text.toString()

        if(email == "" || password == ""){
            Toast.makeText(this@SignInActivity, "Email'inizi ve şifrenizi giriniz!", Toast.LENGTH_LONG).show()
        }else{
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    getUserData()
                    Toast.makeText(this, "Hoş geldiniz :)", Toast.LENGTH_LONG).show()
                    reload()
                }.addOnFailureListener {
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun getUserData(){
        val currentUser = auth.currentUser!!
        database.child("users").child(currentUser.uid).get().addOnSuccessListener {snapshot ->
            val user = snapshot.getValue(User::class.java)!!
            saveUserInLocal(user)
        }
    }

    private fun saveUserInLocal(user : User){
        sharedPreferences.edit()
            .putString("name", user.name)
            .putString("surname", user.surname)
            .putString("profilePhotoUrl", user.profilePhotoUrl)
            .putString("entryYear", user.entryYear)
            .putString("graduationYear", user.graduationYear)
            .putString("emailAddress", user.emailAddress)
            .putString("educationLevel", user.educationLevel)
            .putString("country", user.country)
            .putString("city", user.city)
            .putString("company", user.company)
            .putString("phoneNumber", user.phoneNumber)
            .apply()
    }
}