package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private val TAG = this::class.java.simpleName
    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val response = IdpResponse.fromResultIntent(it.data)
            if (it.resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                Log.i(
                    TAG,
                    "Successfully signed in user " +
                            "${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )

                onBackPressed()
            } else {
                // Sign in failed. If response is null the user canceled the sign-in flow using
                // the back button. Otherwise check response.getError().getErrorCode() and handle
                // the error.
                Toast.makeText(this, "Login Error Try Again", Toast.LENGTH_LONG).show()
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        val customLayout: AuthMethodPickerLayout =
            AuthMethodPickerLayout.Builder(R.layout.login_layout)
                .setGoogleButtonId(R.id.bt_google)
                .setEmailButtonId(R.id.bt_email) // ...
                .build()
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        val signInIntent: Intent =
            AuthUI.getInstance(FirebaseApp.getInstance()).createSignInIntentBuilder() // ...
                .setAuthMethodPickerLayout(customLayout)
                .setAvailableProviders(providers)
                .build()
        getResult.launch(signInIntent)

    }
}
