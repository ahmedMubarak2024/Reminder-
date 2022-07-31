package com.udacity.project4.util

import com.google.firebase.auth.FirebaseUser
import com.udacity.project4.utils.FirebaseUserLiveData

class FireBaseLiveDataTesting : FirebaseUserLiveData() {

    fun setInternalValue(firebaseUser: FirebaseUser) {
        value = firebaseUser
    }

    override fun onActive() {

    }

    override fun onInactive() {

    }
}