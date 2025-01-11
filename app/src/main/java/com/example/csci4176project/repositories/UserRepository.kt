package com.example.csci4176project.repositories

import android.util.Log
import com.example.csci4176project.models.UserModel
import com.example.csci4176project.util.CurrentUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepository {

    // create a static class that acts as a DB helper in relation to groups
    companion object {

        private val db : DatabaseReference by lazy {
            FirebaseDatabase.getInstance().reference
        }

        /**
         * Function used to create a user in the database
         */
        fun create(userId: String, name: String, email: String, pfp: String, currency: String){
            val user = UserModel(name, email, pfp, currency)
            val userRef = db.child("users").child(userId)
            userRef.setValue(user)
        }

        /**
         * Function used to read details about a specific user in the database, based on userId
         */
        fun read(uid: String, callback: (UserModel?) -> Unit) {
            val userRef = db.child("users").child(uid)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserModel::class.java)
                    callback(user)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                }
            })
        }

        /**
         * Utility function used to fetch every user but the currently signed in one. used when,
         * looking to add members to a group, don't display self
         */
        fun getAllButCurrentUser(uid: String, callback: (List<String>) -> Unit){
            val usersRef = db.child("users")
            val userList = mutableListOf<String>()

            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        // for every user from the users db, check if the user is the same as the curent one
                        if (userSnapshot.key != uid) {
                            userList.add(userSnapshot.key!!)
                        }
                    }
                    callback(userList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                }
            })
        }

        /**
         * Utility function used to search through users based on a string query, excluding the currently
         * logged in user
         */
        fun searchUsersButCurrent(uid: String, query: String, callback: (List<String>) -> Unit) {
            val usersRef = db.child("users")
            val userList = mutableListOf<String>()

            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        val userId = userSnapshot.key
                        // first check if the user is the same as the currently logged in one
                        if (userId != uid) {
                            val userData = userSnapshot.getValue(UserModel::class.java)
                            if (userData != null) {
                                val userEmail = userData.email ?: ""
                                val userName = userData.name ?: ""

                                // compare the search with the user's email and username to display them as a valid search result
                                if (userEmail.contains(query, ignoreCase = true) || userName.contains(query, ignoreCase = true)) {
                                    userList.add(userId!!)
                                }
                            }
                        }
                    }
                    callback(userList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                    callback(emptyList()) // Return empty list in case of error
                }
            })
        }

        /**
         * Function used to get the list of groups a user is currently in
         */
        fun getUsersGroups(uid: String, callback: (List<String>) -> Unit) {
            val usersGroupRef = db.child("users").child(uid).child("groups")
            val groupList = mutableListOf<String>()

            usersGroupRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (groupSnapshot in snapshot.children) {
                        val groupId = groupSnapshot.getValue(String::class.java)
                        if (groupId != null) {
                            groupList.add(groupId)
                        }
                    }
                    callback(groupList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                }
            })
        }

        /**
         * Function used to get a list of groups a user is currently invited to
         */
        fun getUsersInvites(uid: String, callback: (List<String>) -> Unit) {
            val usersGroupRef = db.child("users").child(uid).child("invites")
            val groupList = mutableListOf<String>()

            usersGroupRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (groupSnapshot in snapshot.children) {
                        val groupId = groupSnapshot.getValue(String::class.java)
                        if (groupId != null) {
                            groupList.add(groupId)
                        }
                    }
                    callback(groupList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                }
            })
        }

        /**
         * Function used to update information about a user. Don't need to update all information, can
         * choose what to alter
         */
        fun update(userId: String, name: String? = null, email: String? = null, pfp: String? = null, currency: String? = null) {
            val userUpdates = HashMap<String, Any>()

            // figure out what fields need to be updated based on what was left blank
            name?.let { userUpdates["name"] = it }
            email?.let { userUpdates["email"] = it }
            pfp?.let { userUpdates["pfp"] = it }
            currency?.let { userUpdates["currency"] = it }

            // make the corresponding changes
            val userRef = db.child("users").child(userId)
            userRef.updateChildren(userUpdates).addOnCompleteListener{task->
                if(task.isSuccessful){
                    CurrentUser.setCurrentUser(userId)
                }
            }
        }

        /**
         * Utility function used to add a groups ID reference to the users invite list
         */
        fun inviteToGroup(uid: String, groupId: String) {
            val userRef = db.child("users").child(uid)

            // Retrieve the group from the database
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserModel::class.java)

                    if (user != null) {
                        val updatedInviteList = user.invites.toMutableList()
                        updatedInviteList.add(groupId)

                        userRef.child("invites").setValue(updatedInviteList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                }
            })
        }

        /**
         * Utility function used to add an expense ID under a group ID within the expense mapping
         * in a user. This way, you know which transactions a user is invovled in
         */
        fun addExpenseToGroup(uid: String, groupId: String, expenseId: String){
            val userRef = db.child("users").child(uid).child("expenses").child(groupId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val existingExpense: MutableList<String>

                    // grab list of existing transactions, add the next one, and replace the list
                    if(snapshot.value == null){
                        existingExpense = mutableListOf()
                    }
                    else{
                        existingExpense = snapshot.value as MutableList<String>
                    }

                    existingExpense.add(expenseId)
                    userRef.setValue(existingExpense)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                }
            })
        }


        /**
         * Utility function used to convert a group from the invite list of a user to the groups
         * list
         */
        fun acceptInvite(uid: String, groupId: String){
            val userRef = db.child("users").child(uid)

            // Retrieve the group from the database
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserModel::class.java)

                    // Check if group exists and user is in the invite list
                    if (user != null && user.invites.contains(groupId)) {
                        // Remove user from the invite list
                        val updatedInviteList = user.invites.filter { it != groupId }

                        // Add user to the member list
                        val updatedGroupList = user.groups.toMutableList().apply {
                            add(groupId)
                        }

                        // Update the group data in the database
                        userRef.child("invites").setValue(updatedInviteList)
                        userRef.child("groups").setValue(updatedGroupList)

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                }
            })
        }

        /**
         * Utility function used to remove the invite from the user's invite list
         */
        fun declineInvite(uid: String, groupId: String){
            val userRef = db.child("users").child(uid)

            // Retrieve the group from the database
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserModel::class.java)

                    // Check if group exists and user is in the invite list
                    if (user != null && user.invites.contains(groupId)) {
                        // Remove user from the invite list
                        val updatedInviteList = user.invites.filter { it != groupId }

                        // Update the group data in the database
                        userRef.child("invites").setValue(updatedInviteList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                }
            })
        }

        /**
         * Utility function used to remove the groupsID reference from the user's group list
         */
        fun removeGroup(uid: String, groupId: String){
            val userRef = db.child("users").child(uid)

            // Retrieve the user's expenses from the database
            userRef.child("expenses").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expensesMap = snapshot.value as? Map<String, List<String>>

                    // Check if expenses map is not null
                    expensesMap?.let { expenses ->
                        // Remove the group entry from the expenses map
                        val updatedExpensesMap = expenses - groupId

                        // Update the user's expenses in the database
                        userRef.child("expenses").setValue(updatedExpensesMap)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                }
            })

            // Retrieve the group from the database
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserModel::class.java)

                    // Check if group exists and user is in the invite list
                    if (user != null && user.groups.contains(groupId)) {
                        // Remove user from the invite list
                        val updatedGroupList = user.groups.filter { it != groupId }

                        // Update the group data in the database
                        userRef.child("groups").setValue(updatedGroupList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                }
            })
        }

        /**
         * Utility function used to remove an expense under a certain group from the user's
         * expense mapping
         */
        fun removeExpense(uid: String, groupId: String, expenseId: String) {
            val userRef = db.child("users").child(uid).child("expenses").child(groupId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val existingExpense: MutableList<String>

                    // create a list of the expenses already within a group, then delete the corresponding entry
                    if(snapshot.value == null){
                        existingExpense = mutableListOf()
                    }
                    else{
                        existingExpense = snapshot.value as MutableList<String>
                    }

                    existingExpense.remove(expenseId)
                    userRef.setValue(existingExpense)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                }
            })
        }
    }
}