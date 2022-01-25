package com.example.instach.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instach.AccountSettingsActivity
import com.example.instach.MainActivity
import com.example.instach.R
import com.example.instach.ShowUsersActivity
import com.example.instach.adapter.MyImagesAdapter
import com.example.instach.databinding.FragmentProfileBinding
import com.example.instach.model.Post
import com.example.instach.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImage.activity
import java.util.*
import kotlin.collections.ArrayList


class ProfileFragment : Fragment(),MyImagesAdapter.OnItemClickListener {

    private lateinit var mStorage: FirebaseStorage
    private lateinit var binding: FragmentProfileBinding

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var postList: List<Post>
    var myImagesAdapter: MyImagesAdapter? = null
    var myImagesAdapterSaved: MyImagesAdapter? = null
    var postListSaved: List<Post>? = null
    var mySavedImg: List<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        mStorage = FirebaseStorage.getInstance();
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        if (pref != null) {
            this.profileId = pref.getString("profileId", "none")!!
        }

        if (profileId == firebaseUser.uid) {
            binding.btnEdit.text = getString(R.string.edit_profile_lbl)
        } else if (profileId != firebaseUser.uid) {
            checkFollowAndFollowingButtonStatus()
        }

        val recyclerViewUploadImages: RecyclerView = binding.rvProfileImages
        recyclerViewUploadImages.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewUploadImages.layoutManager = linearLayoutManager

        postList = ArrayList()
        myImagesAdapter = context?.let {
            MyImagesAdapter(
                it,
                postList as ArrayList<Post>
            )
        }
        recyclerViewUploadImages.adapter = myImagesAdapter

       myImagesAdapter!!.setOnItemClickListener(this)
       // recyclerViewUploadImages.adapter= MyImagesAdapter(requireContext(),postList)
        //recyclerview for saved images

        val recyclerSavedImages: RecyclerView = binding.rvProfileImagesSaved
        recyclerSavedImages.setHasFixedSize(true)
        val linearLayoutManager2: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerSavedImages.layoutManager = linearLayoutManager2

        postListSaved = ArrayList()
        myImagesAdapterSaved = context?.let {
            MyImagesAdapter(
                it,
                postListSaved as ArrayList<Post>
            )
        }
        recyclerSavedImages.adapter = myImagesAdapterSaved

        val uploadedImagesBtn: ImageButton = binding.imagesGridViewBtn
        uploadedImagesBtn.setOnClickListener{
            recyclerSavedImages.visibility = View.GONE
            recyclerViewUploadImages.visibility = View.VISIBLE
        }

        val savedImagesBtn: ImageButton = binding.imagesSaveBtn
        savedImagesBtn.setOnClickListener{
            recyclerSavedImages.visibility = View.VISIBLE
            recyclerViewUploadImages.visibility = View.GONE
        }

        binding.btnEdit.setOnClickListener {
            when (binding.btnEdit.text.toString()) {
                "Edit Profile" ->
                    startActivity(Intent(context, AccountSettingsActivity::class.java))
                "Follow" -> {
                    firebaseUser.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it)
                            .child("Following").child(profileId).setValue(true)
                    }
                    firebaseUser.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it).setValue(true)
                    }
                    addNotification()
                }
                "Following" -> {

                    firebaseUser.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it)
                            .child("Following").child(profileId).removeValue()
                    }
                    firebaseUser.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it).removeValue()

                    }
                }
            }
        }

        binding.tvTotalFollowers.setOnClickListener{
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id",profileId)
            intent.putExtra("title", "followers")
            startActivity(intent)
        }

        binding.tvTotalFollowing.setOnClickListener{
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id",profileId)
            intent.putExtra("title", "following")
            startActivity(intent)
        }

        getFollowers()
        getFollowings()
        userInfo()
        myPhotos()
        getTotalNumberOfPost()
        mySaved()
        return binding.root
    }

    private fun mySaved() {
        mySavedImg = ArrayList()

        val saveRef = FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid)

        saveRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (snapshots in snapshot.children){
                        (mySavedImg as ArrayList<String>).add(snapshots.key!!)
                    }
                    readSavedImagesData()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

    private fun readSavedImagesData() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    (postListSaved as ArrayList<Post>).clear()

                    for (snapshots in snapshot.children){
                        val post = snapshots.getValue(Post::class.java)
                        for (key in mySavedImg!!){
                            if (post!!.getPostId() == key){
                                (postListSaved as ArrayList<Post>).add(post)
                            }
                        }
                    }
                    myImagesAdapterSaved!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun checkFollowAndFollowingButtonStatus() {
        val followingRef = firebaseUser.uid.let {
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it)
                .child("Following")
        }
        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(profileId).exists()) {
                    binding.btnEdit.text = getString(R.string.following_lbl)

                } else {
                    binding.btnEdit.text = getString(R.string.follow_lbl)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.tvTotalFollowers.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getFollowings() {
        val followingRef =
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.tvTotalFollowing.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun myPhotos() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (postList as ArrayList<Post>).clear()

                    for (snapshots in snapshot.children) {
                        val post = snapshots.getValue(Post::class.java)!!

                        if (post.getPublisher().equals(profileId)) {
                            (postList as ArrayList<Post>).add(post)
                            Collections.reverse(postList)
                            myImagesAdapter!!.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun userInfo() {
        val userRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(profileId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {


                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(binding.profileImage)
                    binding.tvProfile.text = user.getUsername()
                    binding.tvFullName.text = user.getFullname()
                    binding.tvBio.text = user.getBio()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()

        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()

        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()

        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    private fun getTotalNumberOfPost() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var postCounter = 0
                    for (snapshots in snapshot.children) {
                        val post = snapshots.getValue(Post::class.java)
                        if (post!!.getPublisher() == profileId) {
                            postCounter++
                        }
                    }
                    binding.tvTotalPosts.text = " $postCounter"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun addNotification() {
        //osoba koja ce primiti obavijest
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications")
            .child(profileId)

        val notiMap = HashMap<String, Any>()
        // osoba koja lajka post
        notiMap["userId"] = firebaseUser.uid
        notiMap["text"] = "started following you"
        notiMap["postId"] = ""
        notiMap["isPost"] = false

        notiRef.push().setValue(notiMap)

    }

    override fun onItemClick(position: Int) {

    }

    override fun onWhatEverClick(position: Int) {
    }

    override fun onDeleteClick(position: Int) {
        val selectedItem: Post = postList[position]
        val selectedKey: String = selectedItem.getPostId()

        val imageRef: StorageReference = mStorage.getReferenceFromUrl(selectedItem.getPostImage())
        imageRef.delete().addOnSuccessListener {
            FirebaseDatabase.getInstance().reference.child("Posts").child(selectedKey).removeValue()
            Toast.makeText(requireContext(), "Item deleted", Toast.LENGTH_SHORT).show()
        }
    }

}