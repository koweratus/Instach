package com.example.instach

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.instach.`interface`.LoginResultCallBacks
import com.example.instach.databinding.ActivitySignInBinding
import com.example.instach.databinding.ActivitySignUpBinding
import com.example.instach.viewModel.LoginViewModel
import com.example.instach.viewModel.LoginViewModelFactory
import com.example.instach.viewModel.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity(), LoginResultCallBacks {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<ActivitySignUpBinding>(this, R.layout.activity_sign_up)
        binding.viewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(this)
        )[LoginViewModel::class.java]


        binding.btnSigninLink.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }


    }

    private fun CreateAccount() {
        val username = binding.etUsernameSignup.text.toString()
        val fullname = binding.etFullnameSignup.text.toString()
        val email = binding.etEmailSignup.text.toString()
        val password = binding.etPasswordLogin.text.toString()


        val progressDialog = ProgressDialog(this@SignUpActivity)
        progressDialog.setTitle("SignUp")
        progressDialog.setMessage("Please wait, this may take a while...")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserInfo(username, fullname, email, progressDialog)
                } else {
                    val message = "An account already exists with that email"
                    Toast.makeText(
                        this,
                        "Error: $message",
                        Toast.LENGTH_LONG
                    ).show()
                    mAuth.signOut()
                    progressDialog.dismiss()
                }
            }
    }

    private fun saveUserInfo(
        username: String,
        fullname: String,
        email: String,
        progressDialog: ProgressDialog
    ) {

        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullname.lowercase()
        userMap["username"] = username.lowercase()
        userMap["email"] = email
        userMap["isAdmin"] = false
        userMap["bio"] = "hey i am using this app"
        userMap["image"] = "gs://instach-c2484.appspot.com/Default Images/profile.png"
        userRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Account has been created successfully",
                        Toast.LENGTH_LONG
                    ).show()


                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserID)
                        .child("Following").child(currentUserID)
                        .setValue(true)


                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    val message = task.exception!!.toString()
                    Toast.makeText(
                        this,
                        "Erorr: $message",
                        Toast.LENGTH_LONG
                    ).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }
    }

    override fun onSuccess(message: String) {
        CreateAccount()

    }

    override fun onError(message: String) {
        Toast.makeText(this,message,Toast.LENGTH_LONG).show()
    }
}
