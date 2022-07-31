package com.udacity.project4.locationreminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.utils.FirebaseUserLiveData
import org.koin.core.KoinComponent
import org.koin.core.get

class LoginViewModel : ViewModel(), KoinComponent {
    fun logOut() {
        FirebaseAuth.getInstance().signOut()
    }

    val authenticationState = get<FirebaseUserLiveData>().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}

enum class AuthenticationState {
    AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
}