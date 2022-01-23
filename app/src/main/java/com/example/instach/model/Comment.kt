package com.example.instach.model

import com.example.instach.NotificationTypeStrategy

class Comment : NotificationTypeStrategy {

    private var comment: String = ""
    private var publisher: String = ""

    constructor()
    constructor(comment: String, publisher: String) {
        this.comment = comment
        this.publisher = publisher
    }

    fun getComment(): String {
        return comment
    }

    fun setComment(comment: String) {
        this.comment = comment
    }

    fun getPublisher(): String {
        return publisher
    }

    fun setPublisher(publisher: String) {
        this.publisher = publisher
    }

    override fun notificationMode(): String {
        return "commented:"
    }
}