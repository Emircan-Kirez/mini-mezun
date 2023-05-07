package com.emircankirez.mobilehomework.bottomnavfragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.emircankirez.mobilehomework.activities.AddPostActivity
import com.emircankirez.mobilehomework.adapter.PostEveryoneAdapter
import com.emircankirez.mobilehomework.adapter.PostUserAdapter
import com.emircankirez.mobilehomework.databinding.FragmentPostBinding
import com.emircankirez.mobilehomework.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class PostFragment : Fragment() {
    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!
    private lateinit var database : DatabaseReference
    private lateinit var auth : FirebaseAuth
    private var switchIsChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        database = Firebase.database.reference

        binding.apply {
            recyclerViewPosts.layoutManager = LinearLayoutManager(requireContext())

            fabAddPost.setOnClickListener {
                val intent = Intent(requireContext(), AddPostActivity::class.java)
                startActivity(intent)
            }

            switchPost.setOnCheckedChangeListener { _, isChecked ->
                switchIsChecked = isChecked
                if(isChecked){
                    txtPostInfo.text = "Senin Gönderilerin"
                    database.child("posts").get().addOnSuccessListener { snapshot ->
                        getUserPost(snapshot)
                    }
                }else{
                    txtPostInfo.text = "Bütün Gönderiler"
                    database.child("posts").get().addOnSuccessListener { snapshot ->
                        getAllPost(snapshot)
                    }
                }
            }

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(switchIsChecked)
                        getUserPost(snapshot)
                    else
                        getAllPost(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            }

            database.child("posts").addValueEventListener(valueEventListener)
        }
    }

    private fun getAllPost(snapshot: DataSnapshot) {
        binding.progressBar.visibility = View.VISIBLE
        val list = ArrayList<Post>()
        for(ds in snapshot.children.reversed()){
            val post = ds.getValue(Post::class.java)!!
            list.add(post)
        }
        val adapter = PostEveryoneAdapter(list)
        binding.recyclerViewPosts.adapter = adapter
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun getUserPost(snapshot: DataSnapshot){
        binding.progressBar.visibility = View.VISIBLE
        val list = ArrayList<Post>()
        for(ds in snapshot.children.reversed()){
            val post = ds.getValue(Post::class.java)!!
            val currentUserUid = auth.currentUser?.uid.toString()
            if(post.userId == currentUserUid){
                list.add(post)
            }
        }
        val adapter = PostUserAdapter(list)
        binding.recyclerViewPosts.adapter = adapter
        binding.progressBar.visibility = View.INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}