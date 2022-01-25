package com.example.instach.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.instach.model.Post
import com.example.instach.R
import com.squareup.picasso.Picasso
import android.view.MotionEvent

import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import com.example.instach.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MyImagesAdapter(private val mContext: Context, mPost: List<Post>) :
    RecyclerView.Adapter<MyImagesAdapter.ViewHolder?>() {

    private var mPost: List<Post>? = null
    private var mListener: OnItemClickListener? = null

    init {
        this.mPost = mPost
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.images_item_layout, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post: Post = mPost!![position]
        Picasso.get().load(post.getPostImage()).into(holder.postImage)
        val handler = Handler()
        val mLongPressed = Runnable { Log.i("", "Long press!") }

        holder.postImage.setOnClickListener{
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("postId", post.getPostId())
            editor.apply()
            it.findNavController()
                .navigate(R.id.action_navigation_profile_to_postDetailsFragment)

        }
        val postsRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid)
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val user = snapshot.getValue(User::class.java)
                if (user!!.getIsAdmin()){
                    holder.postImage.setOnLongClickListener{
                        it.showContextMenu()
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })






    }

    override fun getItemCount(): Int {
        return mPost!!.size
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener, View.OnClickListener, MenuItem.OnMenuItemClickListener {
        var postImage: ImageView = itemView.findViewById(R.id.post_image)

        init {
            itemView.setOnCreateContextMenuListener(this)
            itemView.setOnClickListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu?.setHeaderTitle("Select The Action")
            val delete: MenuItem = menu!!.add(Menu.NONE, 1, 1, "Delete")
            delete.setOnMenuItemClickListener(this)

        }

        override fun onClick(v: View?) {

            if (mListener != null) {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    mListener!!.onItemClick(position)
                }
            }
        }

        override fun onMenuItemClick(item: MenuItem?): Boolean {
            if (mListener != null) {
                val position: Int = adapterPosition
                if (position != RecyclerView.NO_POSITION) {

                    when (item?.itemId) {
                        1 -> {
                            mListener!!.onDeleteClick(position)
                            return true
                        }
                    }
                }
            }
            return false
        }
    }
    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onWhatEverClick(position: Int)
        fun onDeleteClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }
}


