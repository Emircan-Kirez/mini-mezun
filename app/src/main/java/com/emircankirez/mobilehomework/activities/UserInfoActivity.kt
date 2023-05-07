package com.emircankirez.mobilehomework.activities


import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.emircankirez.mobilehomework.R
import com.emircankirez.mobilehomework.databinding.ActivityUserInfoBinding
import com.emircankirez.mobilehomework.model.User


class UserInfoActivity : AppCompatActivity() {
    private lateinit var binding : ActivityUserInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("user", User::class.java)
        } else {
            intent.getParcelableExtra("user") as User?
        }

        if(user == null){
            Toast.makeText(this@UserInfoActivity, "Kullanıcı bilgileri alınamadı!!", Toast.LENGTH_SHORT).show()
            finish()
        }else{
            createSpinner(user)
            binding.apply {
                Glide.with(this@UserInfoActivity).load(user.profilePhotoUrl).into(imgUserProfile)
                spinnerEducation.isEnabled = false
                txtName.text = user.name
                txtSurname.text = user.surname
                txtEntryYear.text = user.entryYear
                txtGraduationYear.text = user.graduationYear
                txtCountry.text = user.country
                txtCity.text = user.city
                txtCompany.text = user.company
                txtEmail.text = user.emailAddress
                txtPhone.text = user.phoneNumber

                btnSendEmail.setOnClickListener {
                    val emailIntent = Intent(Intent.ACTION_SEND)
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(user.emailAddress))
                    //need this to prompts email client only
                    emailIntent.type = "message/rfc822"
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(Intent.createChooser(emailIntent, "Uygulamayı seçiniz:"))
                    }

                }

                btnCall.setOnClickListener {
                    val callIntent = Intent(Intent.ACTION_DIAL)
                    callIntent.data = Uri.parse("tel:" + txtPhone.text.toString().filterNot { it.isWhitespace() })
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(callIntent);
                    }
                }

            }
        }
    }

    private fun createSpinner(user : User){
        ArrayAdapter.createFromResource(
            this@UserInfoActivity,
            R.array.education_levels,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerEducation.adapter = adapter
            val position = adapter.getPosition(user.educationLevel)
            binding.spinnerEducation.setSelection(position)
        }
    }
}