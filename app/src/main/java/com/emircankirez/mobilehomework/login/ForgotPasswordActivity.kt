package com.emircankirez.mobilehomework.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.emircankirez.mobilehomework.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding : ActivityForgotPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnResetPassword.setOnClickListener {
            val email : String = binding.txtEmail.text.toString().trim {it <= ' '}
            if(email.isEmpty()){
                Toast.makeText(this@ForgotPasswordActivity, "Bir email giriniz", Toast.LENGTH_SHORT).show()
            }else{
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this@ForgotPasswordActivity, "Şifre sıfırlama maili başarılı bir şekilde gönderildi.", Toast.LENGTH_LONG).show()
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this@ForgotPasswordActivity, it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
            }
        }
    }
}