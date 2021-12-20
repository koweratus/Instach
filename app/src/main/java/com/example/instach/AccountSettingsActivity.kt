package com.example.instach

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.instach.Model.User
import com.example.instach.databinding.ActivityAccountSettingsBinding
import com.example.instach.databinding.ActivitySignInBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

class AccountSettingsActivity : AppCompatActivity() {

    private var GALLERY_REQUEST_CODE = 1234
    private lateinit var binding: ActivityAccountSettingsBinding
    private var checker = ""
    private lateinit var firebaseUser: FirebaseUser
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        binding.ivSaveProfile.setOnClickListener {
            if (checker == "clicked") {
                uploadImageAndUpdateInfo()
            } else {
                updateUserInfoOnly()
            }
        }

        binding.tvImage.setOnClickListener {
            checker = "clicked"
            pickFromGallery()
        }
        userInfo()
    }

    private fun uploadImageAndUpdateInfo() {



        when{
            binding.etFullname.text.toString() == "" -> {
                Toast.makeText(this, "Please write full name first", Toast.LENGTH_SHORT).show()
            }
            binding.etUsername.text.toString() == "" -> {
                Toast.makeText(this, "Please write username first", Toast.LENGTH_SHORT).show() }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, we are updating your profile...")
                progressDialog.show()

                val fileReference = storageProfilePicRef!!.child(firebaseUser.uid + ".jpg")
                var uploadTask: StorageTask<*>
                uploadTask = fileReference.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>> { task->
                    if( !task.isSuccessful){
                        task.exception?.let{
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileReference.downloadUrl
                }).addOnCompleteListener( OnCompleteListener<Uri> {task->
                    if (task.isSuccessful){
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")
                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] = binding.etFullname.text.toString().lowercase()
                        userMap["username"] = binding.etUsername.text.toString().lowercase()
                        userMap["bio"] = binding.etBio.text.toString().lowercase()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(
                            this,
                            "Account info has been updated successfully",
                            Toast.LENGTH_LONG
                        ).show()
                        val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()

                    }
                    else{
                        progressDialog.dismiss()
                    }
                })
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            GALLERY_REQUEST_CODE-> {
                if (resultCode == Activity.RESULT_OK)
                {
                    data?.data?.let{ uri ->
                        CropImage.activity(uri)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1920, 1080)
                            .setCropShape(CropImageView.CropShape.RECTANGLE)
                            .start(this)
                    }
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ->
            {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK){
                        imageUri = result.uri
                        binding.profileImageView.setImageURI(imageUri)

                }
            }
        }
    }

    private fun pickFromGallery(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun updateUserInfoOnly() {

        when {
            binding.etFullname.text.toString() == "" -> {
                Toast.makeText(this, "Please write full name first", Toast.LENGTH_SHORT).show()
            }
            binding.etUsername.text.toString() == "" -> {
                Toast.makeText(this, "Please write username first", Toast.LENGTH_SHORT).show()
            }
            else -> {

                val userRef =
                    FirebaseDatabase.getInstance().reference.child("Users")

                val userMap = HashMap<String, Any>()
                userMap["fullname"] = binding.etFullname.text.toString().lowercase()
                userMap["username"] = binding.etUsername.text.toString().lowercase()
                userMap["bio"] = binding.etBio.text.toString().lowercase()

                userRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(
                    this,
                    "Account info has been updated successfully",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }


    private fun userInfo() {
        val userRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {


                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(binding.profileImageView)
                    binding.etUsername.setText(user.getUsername())
                    binding.etFullname.setText(user.getFullname())
                    binding.etBio.setText(user.getBio())
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}