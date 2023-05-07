package com.emircankirez.mobilehomework.bottomnavfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.emircankirez.mobilehomework.R
import com.emircankirez.mobilehomework.adapter.UserAdapter
import com.emircankirez.mobilehomework.databinding.FragmentGraduatedBinding
import com.emircankirez.mobilehomework.model.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class GraduatedFragment : Fragment() {
    private var _binding: FragmentGraduatedBinding? = null
    private val binding get() = _binding!!
    private lateinit var database : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentGraduatedBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = Firebase.database.reference

        createSpinner()

        binding.apply {
            btnSearch.setOnClickListener { search(it) }
            // başlangıçta filtre ve arama kapalı - bütün kullanıcılar gösterilir
            spinnerFilter.isEnabled = false
            btnSearch.isClickable = false
            txtSearch.isEnabled = false

            recyclerViewUsers.layoutManager = LinearLayoutManager(requireContext())
            getUsers()
            cbFilter.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked){
                    cbFilter.text = "Filtre: Açık"
                    spinnerFilter.isEnabled = true
                    btnSearch.isClickable = true
                    txtSearch.isEnabled = true
                    btnSearch.setImageResource(R.drawable.baseline_search_24)
                }else{
                    cbFilter.text = "Filtre: Kapalı"
                    txtSearch.setText("")
                    spinnerFilter.isEnabled = false
                    btnSearch.isClickable = false
                    txtSearch.isEnabled = false
                    btnSearch.setImageResource(R.drawable.baseline_search_off_24)
                    // filtre yokken bütün herkesi göster
                    getUsers()
                }
            }

        }
    }

    private fun getUsers(){
        binding.progressBar.visibility = View.VISIBLE
        database.child("users").get().addOnSuccessListener { snapshot->
            val users = ArrayList<User>()
            for(ds in snapshot.children){
                val user = ds.getValue(User::class.java)!!
                users.add(user)
            }
            val adapter = UserAdapter(users)
            binding.recyclerViewUsers.adapter = adapter
        }
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun search(view : View){
        binding.apply {
            val searchMessage = txtSearch.text.toString()
            when (spinnerFilter.selectedItem.toString()) {
                "Giriş Yılı" -> searchByEntryYear(searchMessage)
                "Mezun Yılı" -> searchByGraduationYear(searchMessage)
                "Şehir" -> searchByCity(searchMessage)
            }

        }
    }

    private fun searchByCity(searchMessage: String) {
        binding.progressBar.visibility = View.VISIBLE
        database.child("users").get().addOnSuccessListener { snapshot->
            val users = ArrayList<User>()
            for(ds in snapshot.children){
                val user = ds.getValue(User::class.java)!!
                if(user.city == searchMessage)
                    users.add(user)
            }
            val adapter = UserAdapter(users)
            binding.recyclerViewUsers.adapter = adapter
        }
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun searchByGraduationYear(searchMessage: String) {
        binding.progressBar.visibility = View.VISIBLE
        database.child("users").get().addOnSuccessListener { snapshot->
            val users = ArrayList<User>()
            for(ds in snapshot.children){
                val user = ds.getValue(User::class.java)!!
                if(user.graduationYear == searchMessage)
                    users.add(user)
            }
            val adapter = UserAdapter(users)
            binding.recyclerViewUsers.adapter = adapter
        }
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun searchByEntryYear(searchMessage: String) {
        binding.progressBar.visibility = View.VISIBLE
        database.child("users").get().addOnSuccessListener { snapshot->
            val users = ArrayList<User>()
            for(ds in snapshot.children){
                val user = ds.getValue(User::class.java)!!
                if(user.entryYear == searchMessage)
                    users.add(user)
            }
            val adapter = UserAdapter(users)
            binding.recyclerViewUsers.adapter = adapter
        }
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun createSpinner(){
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.filters,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerFilter.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}