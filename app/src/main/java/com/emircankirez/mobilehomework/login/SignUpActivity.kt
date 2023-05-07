package com.emircankirez.mobilehomework.login

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.emircankirez.mobilehomework.activities.MainActivity
import com.emircankirez.mobilehomework.R
import com.emircankirez.mobilehomework.databinding.ActivitySignUpBinding
import com.emircankirez.mobilehomework.model.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*


class SignUpActivity : AppCompatActivity(){
    private lateinit var binding : ActivitySignUpBinding
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionForGalleryLauncher: ActivityResultLauncher<String>
    private lateinit var permissionForCameraLauncher : ActivityResultLauncher<String>
    private var selectedBitmap : Bitmap? = null
    private lateinit var auth : FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // firebase init
        auth = Firebase.auth
        storage = Firebase.storage
        database = Firebase.database.reference

        sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)

        registerLaunchers()
        binding.apply {
            imgUser.setOnClickListener { selectImage(it) }
            btnSignUp.setOnClickListener { signUp(it) }
        }

    }

    private fun registerLaunchers(){
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK && result.data != null){
                val intentFromResult = result.data
                val selectedUri = intentFromResult!!.data
                if(selectedUri != null){
                    if(Build.VERSION.SDK_INT >= 28){
                        val source = ImageDecoder.createSource(contentResolver, selectedUri)
                        selectedBitmap = ImageDecoder.decodeBitmap(source)
                    }else{
                        selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedUri)
                    }
                    binding.imgUser.setImageBitmap(selectedBitmap)
                }
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK && result.data != null){
                val bundle = result.data!!.extras
                bundle?.let {
                    selectedBitmap = bundle.get("data") as Bitmap
                    binding.imgUser.setImageBitmap(selectedBitmap)
                }
            }
        }

        permissionForGalleryLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryLauncher.launch(intentToGallery)
            }else{
                Toast.makeText(this@SignUpActivity, "Galeriden resim alabilmek için izin lazım", Toast.LENGTH_SHORT).show()
            }
        }

        permissionForCameraLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if(result){
                val intentToCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch(intentToCamera)
            }else{
                Toast.makeText(this@SignUpActivity, "Kamerayı kullanabilmek için izin lazım", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun selectImage(view: View){
        val alertBuilder = AlertDialog.Builder(this@SignUpActivity)
        alertBuilder.setTitle("Resim Seç")
        alertBuilder.setMessage("Galeriden ya da kameranızı kullanarak resim seçin")
        alertBuilder.setIcon(R.drawable.baseline_image_24)
        alertBuilder.setPositiveButton("kamera"){ dialog, which ->
            // cameradan resim al
            if(ContextCompat.checkSelfPermission(this@SignUpActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this@SignUpActivity, Manifest.permission.CAMERA)){
                    Snackbar.make(view, "Kamera için izin lazım", Snackbar.LENGTH_INDEFINITE).setAction("İzin ver"){
                        // izin al
                        permissionForCameraLauncher.launch(Manifest.permission.CAMERA)
                    }.show()
                }else{
                    permissionForCameraLauncher.launch(Manifest.permission.CAMERA)
                }
            }else{
                // izin alınmış ise
                val intentToCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch(intentToCamera)
            }
        }

        alertBuilder.setNeutralButton("Kapat"){ dialog, which ->
            dialog.dismiss()
        }

        alertBuilder.setNegativeButton("Galeri"){ dialog, which ->
            // galeriden foto al
            if(ContextCompat.checkSelfPermission(this@SignUpActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this@SignUpActivity, Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view, "Galeri için izin lazım", Snackbar.LENGTH_INDEFINITE).setAction("İzin ver"){
                        // izin al
                        permissionForGalleryLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
                }else{
                    permissionForGalleryLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }else{
                // izin alınmış ise
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryLauncher.launch(intentToGallery)
            }
        }
        alertBuilder.show()
    }

    private fun signUp(view: View){
        if(selectedBitmap != null){
            binding.apply {
                val name = txtName.text.toString()
                val surname = txtSurname.text.toString()
                val email = txtEmail.text.toString()
                val password = txtPassword.text.toString()
                val entryYear = txtEntryYear.text.toString()
                val graduationYear = txtGraduationYear.text.toString()

                if(name == "" || surname == "" || email == "" || password == "" || entryYear == "" || graduationYear == ""){
                    Toast.makeText(this@SignUpActivity, "Boşlukları doldurunuz!", Toast.LENGTH_SHORT).show()
                }else{
                    binding.progressBarSignUp.visibility = View.VISIBLE
                    createUser() // bütün boşuklar doluysa yeni kullanıcı oluştur - fotoğrafını yükle - diğer bilgilerini sakla
                }
            }
        }else{
            Toast.makeText(this, "Bir profil fotoğrafı ekleyiniz!", Toast.LENGTH_SHORT).show()
        }
    }

    // kullanıcı kaydı oluştur
    private fun createUser(){
        val email = binding.txtEmail.text.toString()
        val password = binding.txtPassword.text.toString()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                saveImage()
            }.addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
        binding.progressBarSignUp.visibility = View.INVISIBLE
    }

    // profil fotosu kaydet
    private fun saveImage(){
        val imageName = "${UUID.randomUUID()}.jpg"
        val imageReference = storage.reference.child("userImg").child(imageName)

        val baos = ByteArrayOutputStream()
        selectedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imgData : ByteArray = baos.toByteArray()

        imageReference.putBytes(imgData)
            .addOnSuccessListener {
                imageReference.downloadUrl.addOnSuccessListener {
                    val profilePhotoUrl = it.toString()
                    saveUserInfo(profilePhotoUrl, imageName)
                }.addOnFailureListener {
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener{
                // foto kaydedilmediyse oluşturulan kullanıcıyı sil
                auth.currentUser?.delete()
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }

    // kullanıcının diğer bilgilerini kaydet
    private fun saveUserInfo(profilePhotoUrl : String, imageName : String){
        binding.apply {
            val name = txtName.text.toString()
            val surname = txtSurname.text.toString()
            val email = txtEmail.text.toString()
            val entryYear = txtEntryYear.text.toString()
            val graduationYear = txtGraduationYear.text.toString()

            val user = User(name, surname, profilePhotoUrl, entryYear, graduationYear, email)
            val uid = auth.currentUser?.uid.toString()
            database.child("users").child(uid).setValue(user).addOnSuccessListener{
                Toast.makeText(this@SignUpActivity, "Kayıt başarılı", Toast.LENGTH_SHORT).show()
                saveUserInLocal(user)
                reload()
            }.addOnFailureListener {
                // kullanıcı bilgileri kaydedilemezse kullanıcıyı ve kaydedilen fotoyu sil
                auth.currentUser?.delete()
                storage.reference.child("userImg").child(imageName).delete()
                Toast.makeText(this@SignUpActivity, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
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

    private fun reload(){
        val intent = Intent(this@SignUpActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // sign in ve sign up ekranlarını kapat
        startActivity(intent)
        finish()
    }
}