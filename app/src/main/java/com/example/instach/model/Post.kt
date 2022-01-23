package com.example.instach.model

import com.example.instach.NotificationTypeStrategy

class Post : NotificationTypeStrategy {

    private var postId: String = ""

    private var postImage: String = ""

    private var publisher: String = ""

    private var description: String = ""


    constructor()

    constructor(postId: String, postImage: String, publisher: String, description: String) {
        this.postId = postId
        this.postImage = postImage
        this.publisher = publisher
        this.description = description
    }

    fun getPostId(): String {
        return postId
    }
    fun setPostId(postId: String){
        this.postId = postId
    }

    fun getPostImage(): String {
        return postImage
    }
    fun setPostImage(postImage: String){
        this.postImage = postImage
    }
    fun getPublisher(): String {
        return publisher
    }
    fun setPublisher(publisher: String){
        this.publisher = publisher
    }
    fun getDescription(): String {
        return description
    }
    fun setDescription(description: String){
        this.description = description
    }

    override fun notificationMode(): String {
        return "liked your post"
    }

}