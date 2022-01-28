package com.example.instach.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.instach.MainActivity
import com.example.instach.R
import com.example.instach.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class UserAdapter(
    private var mContext: Context,
    private var mUser: List<User>,
    private var isFragment: Boolean = false,
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {


    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user = mUser[position]
        holder.username.text = user.getUsername()
        holder.fullname.text = user.getFullname()
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile)
            .into(holder.user_profile_image)

        checkFollowingStatus(user.getUid(), holder.follow_btn)

        holder.itemView.setOnClickListener {
            if (isFragment) {
                val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                pref.putString("profileId", user.getUid())
                pref.apply()

                //navController.navigate(R.id.navigation_profile)
                it.findNavController().navigate(R.id.navigation_profile)
            } else {
                val intent = Intent(mContext, MainActivity::class.java)
                intent.putExtra("publisherId", user.getUid())
                mContext.startActivity(intent)

            }

        }


        holder.follow_btn.setOnClickListener {
            if (holder.follow_btn.text.toString() == "Follow") {
                firebaseUser?.uid.let { it ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it.toString())
                        .child("Following").child(user.getUid()).setValue(true)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseUser?.uid.let { it ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUid())
                                        .child("Followers").child(it.toString())
                                        .setValue(true).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                    addNotification(user.getUid())
                }
            } else {
                firebaseUser?.uid.let { it ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it.toString())
                        .child("Following").child(user.getUid()).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseUser?.uid.let { it ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUid())
                                        .child("Followers").child(it.toString())
                                        .removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }
            }
        }

    }

    private fun checkFollowingStatus(uid: String, followBtn: Button) {

        val followingRef = firebaseUser?.uid.let {
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it.toString())
                .child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(uid).exists()) {
                    followBtn.text = mContext.getString(R.string.Following_lbl)
                } else {
                    followBtn.text = mContext.getString(R.string.Follow_lbl)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }


    override fun getItemCount(): Int {
        return mUser.size
    }

     class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var username: TextView = itemView.findViewById(R.id.tv_username_search)
        var fullname: TextView = itemView.findViewById(R.id.tv_fullname_search)
        var user_profile_image: CircleImageView =
            itemView.findViewById(R.id.civ_user_profile_image_search)
        var follow_btn: Button = itemView.findViewById(R.id.btn_follow_search)

    }

    private fun addNotification(userId: String) {
        //osoba koja ce primiti obavijest
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications")
            .child(userId)

        val notiMap = HashMap<String, Any>()
        // osoba koja lajka post
        notiMap["userId"] = firebaseUser!!.uid
        notiMap["text"] = "started following you"
        notiMap["postId"] = ""
        notiMap["isPost"] = false

        notiRef.push().setValue(notiMap)

    }
}

