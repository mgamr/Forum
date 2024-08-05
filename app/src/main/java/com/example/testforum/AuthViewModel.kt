package com.example.testforum

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testforum.data.User
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AuthViewModel : ViewModel() {
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = Firebase.firestore

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    init{
        checkAuthStatus()
    }

    private fun updateUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val email = currentUser.email ?: return

            db.collection("users").document(email).get()
                .addOnSuccessListener { document ->
                    val userData = mutableMapOf<String, Any>()

                    val username = document.getString("username")
                    val displayName = currentUser.displayName
                    val profilePicture = currentUser.photoUrl?.toString()

                    userData["email"] = email
                    username?.let { userData["username"] = it }
                    displayName?.let { userData["displayName"] = it }
                    profilePicture?.let { userData["profilePicture"] = it }

                    _user.value = User(
                        email = email,
                        username = username,
                        displayName = displayName,
                        profilePicture = profilePicture
                    )

                    db.collection("users").document(email).set(userData, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d("AuthViewModel", "User data successfully written!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("AuthViewModel", "Error writing document", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.w("AuthViewModel", "Error getting document", e)
                }
//            val userData = mutableMapOf<String, Any>()
//
//            currentUser.email?.let {
//                userData["email"] = it
//            }
//            currentUser.displayName?.let {
//                userData["username"] = it
//                userData["displayName"] = it
//            }
//            currentUser.photoUrl?.toString()?.let {
//                userData["profilePicture"] = it
//            }
//
//            _user.value = User(
//                email = currentUser.email ?: "",
//                username = currentUser.displayName,
//                displayName = currentUser.displayName,
//                profilePicture = currentUser.photoUrl?.toString()
//            )
//
//            db.collection("users").document(currentUser.email ?: "").set(userData, SetOptions.merge())
//                .addOnSuccessListener {
//                    Log.d("AuthViewModel", "User data successfully written!")
//                }
//                .addOnFailureListener { e ->
//                    Log.w("AuthViewModel", "Error writing document", e)
//                }
        }
    }

    fun checkAuthStatus(){
        if(auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
            updateUser()
        }
    }

    fun login(email:String, password:String){
        _authState.value = AuthState.Loading
        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{task ->
                if(task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                    updateUser()
                } else {
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }

    fun signup(email : String,password : String){
        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if (task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                    val newUser = User(
                        email = email,
                        username = "",
                        displayName = "",
                        profilePicture = ""
                    )

                    db.collection("users").document(email).set(newUser, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d("AuthViewModel", "User data successfully written!")
                            _user.value = newUser
                        }
                        .addOnFailureListener { e ->
                            Log.w("AuthViewModel", "Error writing document", e)
                        }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }

    fun signout(googleSignInClient: GoogleSignInClient){
//        auth.signOut()
//        _authState.value = AuthState.Unauthenticated
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            _authState.value = AuthState.Unauthenticated
            _user.value = null
        }
    }

    fun signInWithCredential(credential: AuthCredential) {
        _authState.value = AuthState.Loading
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                    updateUser()
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Authentication failed")
                }
            }
    }

    fun resetPassword(email: String) {
        if (email.isEmpty()) {
            _authState.value = AuthState.Error("Email can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Message("Password reset email sent")
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Failed to send reset email")
                }
            }
    }
}

sealed class AuthState{
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
    data class Message(val message: String) : AuthState()
}