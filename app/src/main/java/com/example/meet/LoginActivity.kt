package com.example.meet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.example.meet.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    lateinit var binding : ActivityLoginBinding

    var googleSigninInclient : GoogleSignInClient? = null

    private lateinit var activityLauncher : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        var gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString((R.string.default_web_client_id)))
                .requestEmail()
                .build()

        googleSigninInclient = GoogleSignIn.getClient(this, gso)

        activityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    var task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        var account = task.getResult(ApiException::class.java)!!
                        firebaseAuthWithGoogle(account)
                        Log.e("GoogleLogin", "fireBaseAuthWithGoogle:" + account.id)
                    } catch (e: ApiException) {
                        Log.e("GoogleLogin", "Google sign in failed" + e.message)
                    }
                }

            }
        binding.loginBtn.setOnClickListener {
            activityLauncher.launch(googleSigninInclient!!.signInIntent)
        }
    }
    fun firebaseAuthWithGoogle(account : GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    saveUserDataToDatabase(task.result!!.user)
                }else
                    println("실패")
            }

    }

    fun saveUserDataToDatabase(user : FirebaseUser?){
        val email : String? = user?.email
        val uid : String? = user?.uid

        var userDTO = UserDTO()
        userDTO.email = email

        FirebaseFirestore.getInstance().collection("users").document(uid!!).set(userDTO)
        finish()
        startActivity(Intent(this,MainActivity::class.java))

    }
}