package com.example.instach

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instach.adapter.UserAdapter
import com.example.instach.model.User
import com.example.instach.databinding.ActivityShowUsersBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowUsersBinding

    var id: String = ""
    var title: String = ""

    var userAdapter: UserAdapter? = null
    var userList: List<User>? = null
    var userIdList: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowUsersBinding.inflate(layoutInflater)
        val intent = intent
        id = intent.getStringExtra("id").toString()
        title = intent.getStringExtra("title").toString()

        val toolbar: androidx.appcompat.widget.Toolbar = binding.showUsersToolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        var recyclerView: RecyclerView
        recyclerView = binding.rv
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(this, userList as ArrayList <User>, false)
        recyclerView.adapter = userAdapter

        userIdList = ArrayList()

        when (title){
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
            "views" -> getViews()

        }

        setContentView(binding.root)

    }

    private fun getViews() {

    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (userIdList as ArrayList<String>).clear()

                for (snapshots in snapshot.children){
                    (userIdList as ArrayList<String>).add(snapshots.key!!)
                }
                showUsers()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getFollowing() {
        val followingRef =
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(id)
                .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (userIdList as ArrayList<String>).clear()

                for (snapshots in snapshot.children){
                    (userIdList as ArrayList<String>).add(snapshots.key!!)
                }
                showUsers()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getLikes() {

        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes")
            .child(id)

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (userIdList as ArrayList<String>).clear()

                    for (snapshots in snapshot.children){
                        (userIdList as ArrayList<String>).add(snapshots.key!!)
                    }
                    showUsers()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun showUsers() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                    (userList as ArrayList<User>).clear()

                    for (snapshot in dataSnapshot.children){
                        val user = snapshot.getValue(User:: class.java)
                        for (id in userIdList!!){
                            if (user!!.getUid() == id){
                                (userList as ArrayList<User>).add(user)
                            }
                        }

                    }
                    userAdapter?.notifyDataSetChanged()
                }


            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}