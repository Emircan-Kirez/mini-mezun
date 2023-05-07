package com.emircankirez.mobilehomework.activities

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.emircankirez.mobilehomework.R
import com.emircankirez.mobilehomework.databinding.ActivityAddAnnouncementBinding
import com.emircankirez.mobilehomework.model.Announcement
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.UUID

class AddAnnouncementActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var binding : ActivityAddAnnouncementBinding
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionForGalleryLauncher: ActivityResultLauncher<String>
    private lateinit var permissionForCameraLauncher : ActivityResultLauncher<String>
    private var selectedBitmap : Bitmap? = null
    private lateinit var auth : FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        storage = Firebase.storage
        database = Firebase.database.reference

        registerLaunchers()
        binding.apply {
            txtDatePicker.setOnClickListener { pickDate(it) }
            btnSaveAnnouncement.setOnClickListener { saveAnnouncement(it) }
            imgAnnouncement.setOnClickListener { selectImage(it) }
            txtLastDate.text = SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.btn_back, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.btnBack){
            val intent = Intent(this@AddAnnouncementActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveAnnouncement(view : View){
        val imageName = "${UUID.randomUUID()}.jpg"
        val imageReference = storage.reference.child("announcementImg").child(imageName)

        if(selectedBitmap != null){
            val baos = ByteArrayOutputStream()
            selectedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imgData : ByteArray = baos.toByteArray()

            imageReference.putBytes(imgData)
                .addOnSuccessListener {
                    imageReference.downloadUrl.addOnSuccessListener {
                        val profilePhotoUrl = it.toString()
                        val uid = auth.currentUser?.uid.toString()

                        binding.apply {
                            val announcement = Announcement(
                                txtTitle.text.toString(),
                                profilePhotoUrl,
                                txtContent.text.toString(),
                                txtLastDate.text.toString(),
                                uid
                            )

                            database.child("announcements").push().setValue(announcement).addOnSuccessListener {
                                Toast.makeText(this@AddAnnouncementActivity, "Duyuru kaydedildi", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                Toast.makeText(this@AddAnnouncementActivity, it.localizedMessage, Toast.LENGTH_LONG).show()
                            }
                        }

                        val intent = Intent(this@AddAnnouncementActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener{
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                }
        }else{
            Toast.makeText(this, "Duyurunuz için bir resim seçiniz!!", Toast.LENGTH_SHORT).show()
        }

    }

    // tarih seçimi
    private fun pickDate(view: View){
        val datePicker =  DatePickerDialog(this, this,
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val format = SimpleDateFormat("dd/MM/yyyyy")
        val currentDay = format.format(Calendar.getInstance().time)
        val selectedDay = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
        val currentDate = format.parse(currentDay)!!
        val selectedDate = format.parse(selectedDay)!!
        if(selectedDate >= currentDate){
            binding.txtLastDate.text = selectedDay
        }else{
            Toast.makeText(this, "Geçmiş tarihi seçemezsiniz!!", Toast.LENGTH_LONG).show()
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
                    binding.imgAnnouncement.setImageBitmap(selectedBitmap)
                }
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK && result.data != null){
                val bundle = result.data!!.extras
                bundle?.let {
                    selectedBitmap = bundle.get("data") as Bitmap
                    binding.imgAnnouncement.setImageBitmap(selectedBitmap)
                }
            }
        }

        permissionForGalleryLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryLauncher.launch(intentToGallery)
            }else{
                Toast.makeText(this@AddAnnouncementActivity, "Galeriden resim alabilmek için izin lazım", Toast.LENGTH_SHORT).show()
            }
        }

        permissionForCameraLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                val intentToCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch(intentToCamera)
            }else{
                Toast.makeText(this@AddAnnouncementActivity, "Kamerayı kullanabilmek için izin lazım", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun selectImage(view: View){
        val alertBuilder = AlertDialog.Builder(this@AddAnnouncementActivity)
        alertBuilder.setTitle("Resim Seç")
        alertBuilder.setMessage("Galeriden ya da kameranızı kullanarak resim seçin")
        alertBuilder.setIcon(R.drawable.baseline_image_24)
        alertBuilder.setPositiveButton("Kamera"){ dialog, which ->
            // cameradan resim al
            if(ContextCompat.checkSelfPermission(this@AddAnnouncementActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this@AddAnnouncementActivity, Manifest.permission.CAMERA)){
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
            if(ContextCompat.checkSelfPermission(this@AddAnnouncementActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this@AddAnnouncementActivity, Manifest.permission.READ_EXTERNAL_STORAGE)){
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
}