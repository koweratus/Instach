package com.example.instach.model

import android.text.TextUtils
import android.util.Patterns
import androidx.databinding.BaseObservable

class User : BaseObservable {
    private var username: String = ""
    private var fullname: String = ""
    private var bio: String = ""
    private var image: String = ""
    private var email: String = ""
    private var password: String = ""
    private var uid: String = ""
    private var isAdmin: Boolean = false

    constructor()

    constructor(
        username: String,
        fullname: String,
        bio: String,
        image: String,
        uid: String,
        email: String,
        password: String
    ) {
        this.username = username
        this.fullname = fullname
        this.bio = bio
        this.image = image
        this.uid = uid
        this.email = email
        this.password = password
    }

    fun getEmail(): String {
        return email
    }

    fun setEmail(email: String) {
        this.email = email
    }

    fun getPassword(): String {
        return password
    }

    fun setPassword(password: String) {
        this.password = password
    }

    fun getUsername(): String {
        return username
    }

    fun setUsername(username: String) {
        this.username = username
    }

    fun getFullname(): String {
        return fullname
    }

    fun setFullname(fullname: String) {
        this.fullname = fullname
    }

    fun getImage(): String {
        return image
    }

    fun setImage(image: String) {
        this.image = image
    }

    fun getBio(): String {
        return bio
    }

    fun setBio(bio: String) {
        this.bio = bio
    }

    fun getUid(): String {
        return uid
    }

    fun setUid(uid: String) {
        this.uid = uid
    }


    fun getIsAdmin(): Boolean {
        return isAdmin
    }

    fun setIsAdmin(isAdmin: Boolean) {
        this.isAdmin = isAdmin
    }

    fun isDataValid(): Int {
        if (TextUtils.isEmpty(getEmail())) {
            return 0
        } else if (!Patterns.EMAIL_ADDRESS.matcher(getEmail()).matches()) {
            return 1
        } else if (getPassword().length <= 7) {
            return 2
        } else if (TextUtils.isEmpty(getUsername())) {
            return 3
        } else if (TextUtils.isEmpty(getFullname())) {
            return 4
        } else {
            return -1
        }
    }

}