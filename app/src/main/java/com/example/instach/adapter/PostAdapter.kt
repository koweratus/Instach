package com.example.instach.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.instach.CommentsActivity
import com.example.instach.MainActivity
import com.example.instach.model.Post
import com.example.instach.model.User
import com.example.instach.R
import com.example.instach.ShowUsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class PostAdapter(private val mContext: Context, private val mPost: List<Post>) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {

        var profileImage: CircleImageView
        var postImage: ImageView
        var likeBtn: ImageView
        var commentBtn: ImageView
        var saveButton: ImageView
        var userName: TextView
        var likes: TextView
        var publisher: TextView
        var description: TextView
        var comments: TextView

        init {
            profileImage = itemView.findViewById(R.id.user_profile_image_post)
            postImage = itemView.findViewById(R.id.post_image_home)
            likeBtn = itemView.findViewById(R.id.post_image_like_btn)
            commentBtn = itemView.findViewById(R.id.post_image_comment_btn)
            saveButton = itemView.findViewById(R.id.post_save_comment_btn)
            userName = itemView.findViewById(R.id.user_name_search)
            likes = itemView.findViewById(R.id.likes)
            publisher = itemView.findViewById(R.id.publisher)
            description = itemView.findViewById(R.id.description)
            comments = itemView.findViewById(R.id.comments)


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.post_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        firebaseUser = FirebaseAuth.getInstance().currentUser
        val post = mPost[position]
        Picasso.get().load(post.getPostImage()).into(holder.postImage)

        if (post.getDescription().equals("")) {
            holder.description.visibility = View.GONE
        } else {
            holder.description.visibility = View.VISIBLE
            holder.description.text = post.getDescription()
        }

        holder.likes.setOnClickListener{
            val intent = Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id",post.getPostId())
            intent.putExtra("title", "likes")
            mContext.startActivity(intent)
        }

        publisherInfo(holder.profileImage, holder.userName, holder.publisher, post.getPublisher())
        isLikes(post.getPostId(), holder.likeBtn)

        numberOfLikes(holder.likes, post.getPostId())
        getTotalNumberComment(holder.comments, post.getPostId())
        checkSavedStatus(post.getPostId(), holder.saveButton)

        holder.likeBtn.setOnClickListener {
            if (holder.likeBtn.tag == "Like") {
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.getPostId())
                    .child(firebaseUser!!.uid)
                    .setValue(true)
                addNotification(post.getPublisher(),post.getPostId())
            } else {
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.getPostId())
                    .child(firebaseUser!!.uid)
                    .removeValue()

                val intent = Intent(mContext, MainActivity::class.java)
                mContext.startActivity(intent)

            }
        }

        holder.commentBtn.setOnClickListener {
            val intentComment = Intent(mContext, CommentsActivity::class.java)
            intentComment.putExtra("postId", post.getPostId())
            intentComment.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intentComment)

        }

        holder.comments.setOnClickListener {
            val intentComment = Intent(mContext, CommentsActivity::class.java)
            intentComment.putExtra("postId", post.getPostId())
            intentComment.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intentComment)

        }

        holder.saveButton.setOnClickListener {

            if (holder.saveButton.tag == "Save"){
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)
                    .child(post.getPostId()).setValue(true)
            }else{
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)
                    .child(post.getPostId()).removeValue()
            }

        }
    }

    private fun numberOfLikes(likes: TextView, postId: String) {
        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes")
            .child(postId)

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (snapshot.childrenCount.toString() + " likes").also { likes.text = it }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getTotalNumberComment(comments: TextView, postId: String) {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postId)

        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    ("view all " + snapshot.childrenCount.toString() + " comments").also { comments.text = it }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun isLikes(postId: String, likeBtn: ImageView) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes")
            .child(postId)

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(firebaseUser!!.uid).exists()) {
                    likeBtn.setImageResource(R.drawable.heart_clicked)
                    likeBtn.tag = mContext.getString(R.string.Liked_lbl)
                } else {
                    likeBtn.setImageResource(R.drawable.heart_not_clicked)
                    likeBtn.tag = mContext.getString(R.string.Like_lbl)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun publisherInfo(
        profileImage: CircleImageView,
        userName: TextView,
        publisher: TextView,
        publisherId: String
    ) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profileImage)
                    userName.text = user.getUsername()
                    publisher.text = user.getFullname()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    private fun checkSavedStatus(postId: String, imageView: ImageView){
        val saveRef =
            FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)

        saveRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(postId).exists()){
                    imageView.setImageResource(R.drawable.save_large_icon)
                    imageView.tag = "Saved"
                }
                else
                {
                    imageView.setImageResource(R.drawable.save_unfilled_large_icon)
                    imageView.tag = "Save"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun addNotification(userId: String, postId: String)
    {
        //osoba koja ce primiti obavijest
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications")
            .child(userId)

        val notiMap = HashMap<String, Any>()
        // osoba koja lajka post
        notiMap["userId"] = firebaseUser!!.uid
        notiMap["text"] = "liked your post"
        notiMap["postId"] = postId
        notiMap["isPost"] = true

        notiRef.push().setValue(notiMap)

    }

}