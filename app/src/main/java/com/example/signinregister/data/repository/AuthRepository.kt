package com.example.signinregister.data.repository

import com.example.signinregister.data.datastore.UserPreferencesManager
import com.example.signinregister.domain.User
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val preferencesManager: UserPreferencesManager,
    private val googleSignInClient: GoogleSignInClient

) {

    suspend fun register(email: String, password: String, username: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("No se pudo crear usuario"))

            val user = User(
                uid = firebaseUser.uid,
                email = email,
                username = username
            )

            db.collection("users").document(firebaseUser.uid).set(user).await()
            preferencesManager.saveUserSession(firebaseUser.uid, email, username)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Usuario no encontrado"))

            val userDoc = db.collection("users").document(firebaseUser.uid).get().await()
            val user = userDoc.toObject(User::class.java) ?:
            User(firebaseUser.uid, firebaseUser.email ?: email)

            preferencesManager.saveUserSession(user.uid, user.email, user.username)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Google login fallido"))

            val userUid = firebaseUser.uid
            val existingUserDoc = db.collection("users").document(userUid).get().await()

            val user: User

            if (existingUserDoc.exists()) {
                user = existingUserDoc.toObject(User::class.java) ?: User(userUid, firebaseUser.email ?: "")

            } else {
                val displayName = firebaseUser.displayName ?: ""

                user = User(
                    uid = userUid,
                    email = firebaseUser.email ?: "",
                    username = displayName,
                    createdAt = System.currentTimeMillis()
                )

                db.collection("users").document(user.uid).set(user).await()
            }

            preferencesManager.saveUserSession(user.uid, user.email, user.username)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithFacebook(accessToken: AccessToken): Result<User> {
        return try {
            val credential = FacebookAuthProvider.getCredential(accessToken.token)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Facebook login fallido"))

            val userUid = firebaseUser.uid
            val existingUserDoc = db.collection("users").document(userUid).get().await()
            val user: User

            if (existingUserDoc.exists()) {
                user = existingUserDoc.toObject(User::class.java) ?: User(userUid, firebaseUser.email ?: "")
            } else {
                val displayName = firebaseUser.displayName ?: ""

                user = User(
                    uid = userUid,
                    email = firebaseUser.email ?: "",
                    username = displayName,
                    createdAt = System.currentTimeMillis()
                )

                db.collection("users").document(user.uid).set(user).await()
            }

            preferencesManager.saveUserSession(user.uid, user.email, user.username)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isUserLoggedIn(): Flow<Boolean> = preferencesManager.isLoggedInFlow

    suspend fun logout() {
        auth.signOut()
        googleSignInClient.signOut().await()
        LoginManager.getInstance().logOut()
        preferencesManager.clearSession()
    }

    fun getUsernameFlow(): Flow<String> {
        return preferencesManager.userNameFlow
    }

    fun getFullNameFlow(): Flow<String> {
        return preferencesManager.fullNameFlow
    }

    fun getGenderFlow(): Flow<String> {
        return preferencesManager.genderFlow
    }

    fun getPhoneFlow(): Flow<String> {
        return preferencesManager.phoneFlow
    }

    fun getEmailFlow(): Flow<String> {
        return preferencesManager.userEmailFlow
    }

    suspend fun updateUsername(newUsername: String): Result<Unit> {
        val userUid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            db.collection("users").document(userUid)
                .update("username", newUsername)
                .await()

            preferencesManager.updateUsername(newUsername)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFullName(newFullName: String): Result<Unit> {
        val userUid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            db.collection("users").document(userUid)
                .update("fullName", newFullName)
                .await()

            preferencesManager.updateFullName(newFullName)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGender(newGender: String): Result<Unit> {
        val userUid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            db.collection("users").document(userUid)
                .update("gender", newGender)
                .await()

            preferencesManager.updateGender(newGender)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePhone(newPhone: String): Result<Unit> {
        val userUid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            db.collection("users").document(userUid)
                .update("phone", newPhone)
                .await()

            preferencesManager.updatePhone(newPhone)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEmail(newEmail: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            user.updateEmail(newEmail).await()

            db.collection("users").document(user.uid)
                .update("email", newEmail)
                .await()

            preferencesManager.updateEmail(newEmail)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}