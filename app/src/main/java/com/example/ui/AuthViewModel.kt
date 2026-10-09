package com.example.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val currentUser: FirebaseUser? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(AuthUiState(currentUser = auth.currentUser))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _uiState.update { it.copy(currentUser = firebaseAuth.currentUser, isLoading = false) }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    fun signInWithGoogle(activity: Activity) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(activity)
                val serverClientId = activity.getString(R.string.default_web_client_id)

                val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = activity,
                    request = request
                )

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                    val authResult = auth.signInWithCredential(firebaseCredential).await()
                    _uiState.update { it.copy(currentUser = authResult.user, isLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Unexpected credential format received"
                        )
                    }
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d(TAG, "User cancelled Google Sign-in flow", e)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-in failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Google Sign-in failed"
                    )
                }
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _uiState.update { it.copy(currentUser = null) }
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}
