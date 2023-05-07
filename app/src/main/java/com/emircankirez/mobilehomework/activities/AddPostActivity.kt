package com.emircankirez.mobilehomework.activities

import android.Manifest
import android.app.AlertDialog
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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.emircankirez.mobilehomework.R
import com.emircankirez.mobilehomework.databinding.ActivityAddPostBinding
import com.emircankirez.mobilehomework.model.Announcement
import com.emircankirez.mobilehomework.model.Post
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

class AddPostActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAddPostBinding
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
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        storage = Firebase.storage
        database = Firebase.database.reference

        registerLaunchers()
        binding.apply {
            btnSharePost.setOnClickListener { sharePost(it) }
            imgPost.setOnClickListener { selectImage(it) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.btn_back, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.btnBack){
            val intent = Intent(this@AddPostActivity, MainActivity::class.java)
            intent.putExtra("info", "post")
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sharePost(view : View){
        val imageName = "${UUID.randomUUID()}.jpg"
        val imageReference = storage.reference.child("postImg").child(imageName)

        if(selectedBitmap != null){
            val baos = ByteArrayOutputStream()
            selectedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imgData : ByteArray = baos.toByteArray()

            imageReference.putBytes(imgData)
                .addOnSuccessListener {
                    imageReference.downloadUrl.addOnSuccessListener {
                        val postPhotoUrl = it.toString()
                        val uid = auth.currentUser?.uid.toString()
                        val postReference = database.child("posts").push()

                        binding.apply {
                            val post = Post(
                                postReference.key!!,
                                txtPostCaption.text.toString(),
                                postPhotoUrl,
                                uid,
                                SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time)
                            )

                            postReference.setValue(post).addOnSuccessListener {
                                Toast.makeText(this@AddPostActivity, "Gönderi paylaşıldı", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                Toast.makeText(this@AddPostActivity, it.localizedMessage, Toast.LENGTH_LONG).show()
                            }
                        }

                        val intent = Intent(this@AddPostActivity, MainActivity::class.java)
                        intent.putExtra("info", "post")
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener{
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                }
        }else{
            Toast.makeText(this, "Gönderiniz için bir resim seçiniz!!", Toast.LENGTH_SHORT).show()
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
                    binding.imgPost.setImageBitmap(selectedBitmap)
                }
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK && result.data != null){
                val bundle = result.data!!.extras
                bundle?.let {
                    selectedBitmap = bundle.get("data") as Bitmap
                    binding.imgPost.setImageBitmap(selectedBitmap)
                }
            }
        }

        permissionForGalleryLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryLauncher.launch(intentToGallery)
            }else{
                Toast.makeText(this@AddPostActivity, "Galeriden resim alabilmek için izin lazım", Toast.LENGTH_SHORT).show()
            }
        }

        permissionForCameraLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                val intentToCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch(intentToCamera)
            }else{
                Toast.makeText(this@AddPostActivity, "Kamerayı kullanabilmek için izin lazım", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun selectImage(view: View){
        val alertBuilder = AlertDialog.Builder(this@AddPostActivity)
        alertBuilder.setTitle("Resim Seç")
        alertBuilder.setMessage("Galeriden ya da kameranızı kullanarak resim seçin")
        alertBuilder.setIcon(R.drawable.baseline_image_24)
        alertBuilder.setPositiveButton("Kamera"){ dialog, which ->
            // cameradan resim al
            if(ContextCompat.checkSelfPermission(this@AddPostActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this@AddPostActivity, Manifest.permission.CAMERA)){
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
            if(ContextCompat.checkSelfPermission(this@AddPostActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this@AddPostActivity, Manifest.permission.READ_EXTERNAL_STORAGE)){
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