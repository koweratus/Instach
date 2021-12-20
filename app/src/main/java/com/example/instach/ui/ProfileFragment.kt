package com.example.instach.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.instach.AccountSettingsActivity
import com.example.instach.Model.User
import com.example.instach.R
import com.example.instach.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)


        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        if (pref != null) {
            this.profileId = pref.getString("profileId", "none")!!
        }

        if (profileId == firebaseUser.uid) {
            binding.btnEdit.text = "Edit Profile"
        } else if (profileId != firebaseUser.uid) {
            checkFollowAndFollowingButtonStatus()
        }

        binding.btnEdit.setOnClickListener {
            val getButtonText = binding.btnEdit.text.toString()
            when {
                getButtonText == "Edit Profile" ->
                    startActivity(Intent(context, AccountSettingsActivity::class.java))

                getButtonText == "Follow" -> {
                    firebaseUser.uid.let { it ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it.toString())
                            .child("Following").child(profileId).setValue(true)
                    }
                    firebaseUser.uid.let { it ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it.toString()).setValue(true)
                    }

                }

                getButtonText == "Following" -> {

                    firebaseUser.uid.let { it ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it.toString())
                            .child("Following").child(profileId).removeValue()
                    }
                    firebaseUser.uid.let { it ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it.toString()).removeValue()

                    }
                }
            }
        }
            getFollowers()
            getFollowings()
            userInfo()
            return binding.root
        }

        private fun checkFollowAndFollowingButtonStatus() {
            val followingRef = firebaseUser.uid.let { it ->
                FirebaseDatabase.getInstance().reference
                    .child("Follow").child(it.toString())
                    .child("Following")
            }
            followingRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(profileId).exists()) {
                        binding.btnEdit.text = "Following"

                    } else {
                        binding.btnEdit.text = "Follow"
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

    }