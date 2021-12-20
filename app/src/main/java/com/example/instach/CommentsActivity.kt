package com.example.instach

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instach.Adapter.CommentsAdapter
import com.example.instach.Model.Comment
import com.example.instach.Model.Post
import com.example.instach.Model.User
import com.example.instach.databinding.ActivityCommentsBinding
import com.example.instach.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso


class CommentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommentsBinding

    private var postId = ""
    private var publisherId = ""
    private var firebaseUser: FirebaseUser? = null
    private var commentAdapter: CommentsAdapter? = null
    private var commentList: MutableList<Comment>?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsBinding.inflate(layoutInflater)

        val intent = intent
        postId = intent.getStringExtra("postId").toString()
        publisherId = intent.getStringExtra("publisherId").toString()

        firebaseUser = FirebaseAuth.getInstance().currentUser

        var recyclerView : RecyclerView
        recyclerView = binding.rvComments
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager

        commentList = ArrayList()
        commentAdapter = CommentsAdapter(this, commentList as ArrayList<Comment>)
        recyclerView.adapter = commentAdapter

        userInfo()
        readComment()
        getPostImage()

        binding.postComment.setOnClickListener(
            View.OnClickListener {
                if (binding.postComment.text.toString() == ""){
                    Toast.makeText(this, "Please write comment first", Toast.LENGTH_SHORT).show()
                }else{
                    addComment()
                }

            }
        )

        setContentView(binding.root)

    }

    private fun addComment() {
        val commentsRef =
            FirebaseDatabase.getInstance().reference.child("Comments")
                .child(postId)

        val commentsMap = HashMap<String,Any>()
        commentsMap["comment"] = binding.etComment.text.toString()
        commentsMap["publisher"] = firebaseUser!!.uid

        commentsRef.push().setValue(commentsMap)

        binding.etComment.text.clear()

    }


    private fun userInfo() {
        val userRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {


                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(binding.cvImageComment)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getPostImage() {
        val postRef =
            FirebaseDatabase.getInstance().reference.child("Posts").child(postId).child("postImage")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {


                if (snapshot.exists()) {
                    val image = snapshot.value.toString()
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(binding.postImageComment)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun readComment(){
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments")
            .child(postId)
        commentsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for(snapshots in snapshot.children){
                        val comments = snapshots.getValue(Comment::class.java)
                        commentList!!.add(comments!!)
                    }

                    commentAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}

