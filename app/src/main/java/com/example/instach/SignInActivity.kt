package com.example.instach

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.instach.`interface`.LoginResultCallBacks
import com.example.instach.databinding.ActivitySignInBinding
import com.example.instach.viewModel.LoginViewModel
import com.example.instach.viewModel.LoginViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


const val REQUEST_CODE_SIGN_IN = 0

class SignInActivity : AppCompatActivity(), LoginResultCallBacks {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth

    var provider = OAuthProvider.newBuilder("github.com")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        binding = DataBindingUtil.setContentView(this,R.layout.activity_sign_in)
        binding.viewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(this)
        )[LoginViewModel::class.java]


        binding.btnSignupLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.githubLoginBtn.setOnClickListener {
            githubAuthForFirebase()
        }

        binding.googleLoginBtn.setOnClickListener {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.webclient_id))
                .requestEmail()
                .build()


            val signInClient = GoogleSignIn.getClient(this, options)
            signInClient.signInIntent.also {
                startActivityForResult(it, REQUEST_CODE_SIGN_IN)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
            account?.let {
                googleAuthForFirebase(it)
            }
        }
    }

    private fun githubAuthForFirebase() {
        auth
            .startActivityForSignInWithProvider( /* activity= */this, provider.build())
            .addOnSuccessListener {
                saveGoogleInfo(
                    it.additionalUserInfo?.username!!,
                    it.additionalUserInfo?.username!!,
                    it.user?.email!!
                )
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
    }


    private fun googleAuthForFirebase(account: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.signInWithCredential(credentials).await()
                withContext(Dispatchers.Main) {
                    saveGoogleInfo(
                        account.givenName.toString(),
                        account.displayName.toString(),
                        account.email.toString()
                    )
                    val intent = Intent(this@SignInActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()


                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignInActivity, e.message, Toast.LENGTH_LONG).show()
                    auth.signOut()

                }
            }
        }
    }

    private fun loginUser() {

        val email = binding.etEmailLogin.text.toString()
        val password = binding.etPasswordLogin.text.toString()


        val progressDialog = ProgressDialog(this@SignInActivity)
        progressDialog.setTitle("Login")
        progressDialog.setMessage("Please wait, this may take a while...")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this@SignInActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    val message = "this user does not exist"
                    Toast.makeText(
                        this,
                        "Erorr: $message",
                        Toast.LENGTH_LONG
                    ).show()
                    mAuth.signOut()
                    progressDialog.dismiss()
                }
            }
    }


    private fun saveGoogleInfo(
        username: String,
        fullname: String,
        email: String
    ) {

        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullname.lowercase()
        userMap["username"] = username.lowercase()
        userMap["email"] = email
        userMap["bio"] = "hey i am using this app"
        userMap["image"] = "gs://instach-c2484.appspot.com/Default Images/profile.png"
        userRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Account has been created successfully",
                        Toast.LENGTH_LONG
                    ).show()


                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserID)
                        .child("Following").child(currentUserID)
                        .setValue(true)

                } else {
                    val message = task.exception!!.toString()
                    Toast.makeText(
                        this,
                        "Erorr: $message",
                        Toast.LENGTH_LONG
                    ).show()
                    FirebaseAuth.getInstance().signOut()
                }
            }


    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this@SignInActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onSuccess(message: String) {
        loginUser()
    }

    override fun onError(message: String) {
        Toast.makeText(this,message,Toast.LENGTH_LONG).show()
    }
}