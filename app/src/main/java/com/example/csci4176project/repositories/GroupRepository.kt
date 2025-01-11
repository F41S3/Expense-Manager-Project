package com.example.csci4176project.repositories

import android.util.Log
import com.example.csci4176project.models.GroupModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GroupRepository {

    // create a static class that acts as a DB helper in relation to groups
    companion object {
        private val db : DatabaseReference by lazy {
            FirebaseDatabase.getInstance().reference
        }

        /**
         * Function used to create a group in the database. Has to create the group under the group
         * db and add the reference to each user involved
         */
        fun create(name: String, type: String, invites: List<String>):String{
            val groupsRef = db.child("groups")
            val groupID = groupsRef.push().key

            val group = GroupModel(name, type, invites)
            if (groupID != null) {
                groupsRef.child(groupID).setValue(group)
            }

            // invite every member when group is created, give them option to decline or accept
            for(inv in invites){
                UserRepository.inviteToGroup(inv, groupID.toString())
            }

            return groupID.toString()
        }

        /**
         * Function used to read details pertaining a specific group
         */
        fun read(groupId: String, callback: (GroupModel?) -> Unit) {
            val groupRef = db.child("groups").child(groupId)

            groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val group = snapshot.getValue(GroupModel::class.java)
                    callback(group)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                }
            })
        }

        /**
         * Function used to delete a group from the database. Needs to delete all expenses
         * pertaining to that group as well as remove the group reference from every user involved
         * (invited or as a member)
         */
        fun delete(groupId: String) {
            val groupRef = db.child("groups").child(groupId)
            ExpenseRepository.deleteGroup(groupId)


            read(groupId){group->
                group?.let {
                    it.members.forEach {userId ->
                        // for every member, remove them
                        UserRepository.removeGroup(userId, groupId)
                    }

                    it.invites.forEach{inviteId->
                        // for every invitee, remove them
                        UserRepository.declineInvite(inviteId, groupId)
                    }
                }

                // use utility function to delete all expenses for a group
                ExpenseRepository.deleteGroup(groupId)

                // finally, remove the group itself
                groupRef.removeValue()
            }
        }

        /**
         * Function used to get a list of the userIds of all group members
         */
        fun getGroupMembers(groupId: String, callback: (List<String>) -> Unit){
            val groupRef = db.child("groups").child(groupId).child("members")
            val userList = mutableListOf<String>()

            groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // for every user within a group, add them to the list to return in the callback
                    for (userSnapshot in snapshot.children) {
                        val userId = userSnapshot.getValue(String::class.java)
                        if (userId != null) {
                            userList.add(userId)
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
         * Function used to get a list of the userIds of all group invitees
         */
        fun getGroupInvites(groupId: String, callback: (List<String>) -> Unit){
            val groupRef = db.child("groups").child(groupId).child("invites")
            val userList = mutableListOf<String>()

            groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        val userId = userSnapshot.getValue(String::class.java)
                        if (userId != null) {
                            userList.add(userId)
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
         * Function used to formally accept an invite into a group
         */
        fun acceptInvite(uid: String, groupID: String) {
            val groupRef = db.child("groups").child(groupID)
            val userRef = db.child("users").child(uid)

            // Retrieve the group from the database
            groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val group = snapshot.getValue(GroupModel::class.java)

                    // Check if group exists and user is in the invite list
                    if (group != null && group.invites.contains(uid)) {
                        // Remove user from the invite list
                        val updatedInviteList = group.invites.filter { it != uid }

                        // Add user to the member list
                        val updatedMemberList = group.members.toMutableList().apply {
                            add(uid)
                        }

                        // Update the group data in the database
                        groupRef.child("invites").setValue(updatedInviteList)
                        groupRef.child("members").setValue(updatedMemberList)

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                }
            })

            // also accept the invite in the user's own reference
            userRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    UserRepository.acceptInvite(uid, groupID)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                }
            })
        }

        /**
         * Function used to formally decline an invite to a group
         */
        fun declineInvite(uid: String, groupID: String){
            val groupRef = db.child("groups").child(groupID)
            val userRef = db.child("users").child(uid)

            // Retrieve the group from the database
            groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val group = snapshot.getValue(GroupModel::class.java)

                    // Check if group exists and user is in the invite list
                    if (group != null && group.invites.contains(uid)) {
                        // Remove user from the invite list
                        val updatedInviteList = group.invites.filter { it != uid }

                        // Update the group data in the database
                        groupRef.child("invites").setValue(updatedInviteList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                }
            })


            // also remove the invite from the user's invite list
            userRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    UserRepository.declineInvite(uid, groupID)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                }
            })
        }

    }

}