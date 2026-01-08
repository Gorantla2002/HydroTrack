package uk.ac.tees.mad.s3548263.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.s3548263.data.model.User
import uk.ac.tees.mad.s3548263.utils.Resource

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    fun getCurrentUserFlow(): Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }

    suspend fun signUp(email: String, password: String, displayName: String): Resource<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                val userModel = User(
                    userId = user.uid,
                    email = email,
                    displayName = displayName
                )
                firestore.collection("users")
                    .document(user.uid)
                    .set(userModel)
                    .await()
                Resource.Success(user)
            } else {
                Resource.Error("User creation failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sign up failed")
        }
    }

    suspend fun signIn(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("Sign in failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sign in failed")
        }
    }

    suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Password reset failed")
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun deleteAccount(): Resource<Unit> {
        return try {
            val user = currentUser
            if (user != null) {
                firestore.collection("users").document(user.uid).delete().await()
                user.delete().await()
                Resource.Success(Unit)
            } else {
                Resource.Error("No user logged in")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Account deletion failed")
        }
    }
}
