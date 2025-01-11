package com.example.csci4176project.util

import android.util.Log
import com.example.csci4176project.models.UserModel
import com.example.csci4176project.repositories.UserRepository

/**
 * Singleton class used to hold information about the currently logged in user
 */
object CurrentUser {
    var userId: String? = null
    var userObj: UserModel? = null

    var name: String? = null
    var email: String? = null
    var pfp: String? = null
    var currency: String? = null

    // initialize information, called when the user signs up/logs in
    fun setCurrentUser(uid: String) {
        userId = uid
        UserRepository.read(userId!!) { user ->
            if (user != null) {
                userObj = user

                name = userObj?.name
                email = userObj?.email
                pfp = userObj?.pfp
                currency = userObj?.currency
            }
            else{
                Log.d("ERROR", "null user")
            }
        }
    }
}

