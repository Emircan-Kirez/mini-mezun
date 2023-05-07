package com.emircankirez.mobilehomework.bottomnavfragments


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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.emircankirez.mobilehomework.R
import com.emircankirez.mobilehomework.databinding.FragmentProfileBinding
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
import java.util.UUID


class ProfileFragment : Fragment() {
    private var _binding : FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionForGalleryLauncher: ActivityResultLauncher<String>
    private lateinit var permissionForCameraLauncher : ActivityResultLauncher<String>
    private var selectedBitmap : Bitmap? = null
    private lateinit var auth : FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // firebase init
        auth = Firebase.auth
        storage = Firebase.storage
        database = Firebase.database.reference
        sharedPreferences = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE)

        registerLaunchers()
        createSpinner()
        binding.apply {
            imgUserProfile.setOnClickListener { selectImage(view) }
            btnSave.setOnClickListener { saveUserInfo(view) }

            // bütün bilgileri eşle
            Glide.with(this@ProfileFragment).load(sharedPreferences.getString("profilePhotoUrl", "")).into(imgUserProfile)
            txtName.setText(sharedPreferences.getString("name", ""))
            txtSurname.setText(sharedPreferences.getString("surname", ""))
            txtGraduationYear.setText(sharedPreferences.getString("graduationYear", ""))
            txtEntryYear.setText(sharedPreferences.getString("entryYear", ""))
            txtEmail.text = sharedPreferences.getString("emailAddress", "")
            txtCountry.setText(sharedPreferences.getString("country", ""))
            txtCity.setText(sharedPreferences.getString("city", ""))
            txtCompany.setText(sharedPreferences.getString("company", ""))
            txtPhone.setText(sharedPreferences.getString("phoneNumber", ""))
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createSpinner(){
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.education_levels,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerEducation.adapter = adapter
            val position = adapter.getPosition(sharedPreferences.getString("educationLevel", "Lisans"))
            binding.spinnerEducation.setSelection(position)
        }
    }

    private fun saveUserInfo(view: View){
        binding.apply {
            val name = txtName.text.toString()
            val surname = txtSurname.text.toString()
            val entryYear = txtEntryYear.text.toString()
            val graduationYear = txtGraduationYear.text.toString()
            var profilePhotoUrl = sharedPreferences.getString("profilePhotoUrl", "")!!
            val educationLevel = spinnerEducation.selectedItem.toString()
            val country = txtCountry.text.toString()
            val city = txtCity.text.toString()
            val company = txtCompany.text.toString()
            val emailAddress = txtEmail.text.toString()
            val phoneNumber = txtPhone.text.toString()

            if(selectedBitmap != null){
                // yeni resim seçili ise;
                saveImage(profilePhotoUrl)
            }else{
                val user = User(name, surname, profilePhotoUrl, entryYear, graduationYear, emailAddress, educationLevel, country, city, company, phoneNumber)
                saveUserInLocal(user) // bütün yeni bilgileri local'e ve firebase'e kaydet
                val uid = auth.currentUser?.uid.toString()
                database.child("users").child(uid).setValue(user).addOnSuccessListener {
                        Toast.makeText(requireContext(), "Bilgileriniz kaydedildi.", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                    Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }

        }


    }

    // profil fotosu kaydet
    private fun saveImage(oldUrl : String){
        val imageName = "${UUID.randomUUID()}.jpg"
        val imageReference = storage.reference.child("userImg").child(imageName)

        val baos = ByteArrayOutputStream()
        selectedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imgData : ByteArray = baos.toByteArray()

        imageReference.putBytes(imgData)
            .addOnSuccessListener {
                imageReference.downloadUrl.addOnSuccessListener {
                    val profilePhotoUrl = it.toString()
                    val imgReference = storage.getReferenceFromUrl(oldUrl)
                    imgReference.delete()

                    binding.apply {
                        val name = txtName.text.toString()
                        val surname = txtSurname.text.toString()
                        val entryYear = txtEntryYear.text.toString()
                        val graduationYear = txtGraduationYear.text.toString()
                        val educationLevel = spinnerEducation.selectedItem.toString()
                        val country = txtCountry.text.toString()
                        val city = txtCity.text.toString()
                        val company = txtCompany.text.toString()
                        val emailAddress = txtEmail.text.toString()
                        val phoneNumber = txtPhone.text.toString()

                        val user = User(name, surname, profilePhotoUrl, entryYear, graduationYear, emailAddress, educationLevel, country, city, company, phoneNumber)
                        saveUserInLocal(user) // bütün yeni bilgileri local'e ve firebase'e kaydet
                        val uid = auth.currentUser?.uid.toString()
                        database.child("users").child(uid).setValue(user).addOnSuccessListener {
                                Toast.makeText(requireContext(), "Bilgileriniz kaydedildi.", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                            Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                    }

                }.addOnFailureListener {
                    Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener{
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
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

    private fun registerLaunchers(){
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == AppCompatActivity.RESULT_OK && result.data != null){
                val intentFromResult = result.data
                val selectedUri = intentFromResult!!.data
                if(selectedUri != null){
                    if(Build.VERSION.SDK_INT >= 28){
                        val source = ImageDecoder.createSource(requireContext().contentResolver, selectedUri)
                        selectedBitmap = ImageDecoder.decodeBitmap(source)
                    }else{
                        selectedBitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedUri)
                    }
                    binding.imgUserProfile.setImageBitmap(selectedBitmap)
                }
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == AppCompatActivity.RESULT_OK && result.data != null){
                val bundle = result.data!!.extras
                bundle?.let {
                    selectedBitmap = bundle.get("data") as Bitmap
                    binding.imgUserProfile.setImageBitmap(selectedBitmap)
                }
            }
        }

        permissionForGalleryLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryLauncher.launch(intentToGallery)
            }else{
                Toast.makeText(requireContext(), "Galeriden resim alabilmek için izin lazım", Toast.LENGTH_SHORT).show()
            }
        }

        permissionForCameraLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                val intentToCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch(intentToCamera)
            }else{
                Toast.makeText(requireContext(), "Kamerayı kullanabilmek için izin lazım", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun selectImage(view: View){
        val alertBuilder = AlertDialog.Builder(requireContext())
        alertBuilder.setTitle("Resim Seç")
        alertBuilder.setMessage("Galeriden ya da kameranızı kullanarak resim seçin")
        alertBuilder.setIcon(R.drawable.baseline_image_24)
        alertBuilder.setPositiveButton("Camera"){ dialog, which ->
            // cameradan resim al
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)){
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
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
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