package com.example.instach.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.instach.model.Post
import com.example.instach.R
import com.squareup.picasso.Picasso

class MyImagesAdapter(private val mContext: Context, mPost: List<Post>): RecyclerView.Adapter<MyImagesAdapter.ViewHolder?>() {

    private var mPost: List<Post>? = null

    init {
        this.mPost = mPost
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.images_item_layout, parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post: Post = mPost!![position]
        Picasso.get().load(post.getPostImage()).into(holder.postImage)

        holder.postImage.setOnClickListener{ view ->
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("postId", post.getPostId())
            editor.apply()
           view.findNavController().navigate(R.id.action_navigation_profile_to_postDetailsFragment)


        }
    }

    override fun getItemCount(): Int {
        return mPost!!.size
    }

    inner class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView){
        var postImage: ImageView

        init {
            postImage = itemView.findViewById(R.id.post_image)

        }
    }

}