package com.example.csci4176project.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.csci4176project.adapters.MemListAdapter
import com.example.csci4176project.R
import com.example.csci4176project.adapters.SelectUserAdapter
import com.example.csci4176project.models.UserModel
import com.example.csci4176project.repositories.GroupRepository
import com.example.csci4176project.repositories.UserRepository
import com.example.csci4176project.util.CurrentUser

class MemListActivity: AppCompatActivity() {

    private lateinit var backBtn: ImageButton
    private lateinit var groupId: String
    private lateinit var memList: RecyclerView
    private lateinit var adapter1: MemListAdapter
    private lateinit var groupName: TextView

    private lateinit var pendingList: RecyclerView
    private lateinit var adapter2: MemListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members_list)

        groupId = intent.getStringExtra("groupId").toString()

        backBtn = findViewById(R.id.backBtn)
        groupName = findViewById(R.id.textView)
        memList = findViewById(R.id.memList)
        pendingList = findViewById(R.id.pendingList)

        // set existing views
        populateViews()

        backBtn.setOnClickListener{
            finish()
        }


    }

    // function used to set existing views
    private fun populateViews(){
        GroupRepository.read(groupId){group ->
            groupName.setText(group?.name)
            groupName.append(" Members")
        }

        val notifBtn: ImageButton = findViewById(R.id.notificationIcon)
        notifBtn.setOnClickListener{
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        val activityBtn: ImageButton = findViewById(R.id.activityIcon)
        activityBtn.setOnClickListener{
            val intent = Intent(this, TransactionActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        val groupBtn: ImageButton = findViewById(R.id.groupIcon)
        groupBtn.setOnClickListener{
            val intent = Intent(this, GroupsActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        val profileBtn: ImageButton = findViewById(R.id.profileIcon)
        profileBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        // set up adapters for both group list and invite list
        handleAdapter1()
        handleAdapter2()
    }


    // function used to set up list containing current group members
    private fun handleAdapter1(){
        val layoutManager = LinearLayoutManager(this)
        memList.layoutManager = layoutManager
        adapter1 = MemListAdapter()
        memList.adapter = adapter1

        // grab group's member list and display information for every user
        GroupRepository.getGroupMembers(groupId) { userIds ->

            val userList = mutableListOf<UserModel>()
            for (id in userIds) {
                UserRepository.read(id) { user ->
                    if (user != null) {
                        userList.add(user)
                        adapter1.setUsers(userList)
                        adapter1.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    // function used to set up list containing pending invited group members
    private fun handleAdapter2(){
        val layoutManager = LinearLayoutManager(this)
        pendingList.layoutManager = layoutManager
        adapter2 = MemListAdapter()
        pendingList.adapter = adapter2

        // grab group's invite list and display information for every user
        GroupRepository.getGroupInvites(groupId) { userIds ->

            val userList = mutableListOf<UserModel>()
            for (id in userIds) {
                UserRepository.read(id) { user ->
                    if (user != null) {
                        userList.add(user)
                        adapter2.setUsers(userList)
                        adapter2.notifyDataSetChanged()
                    }
                }
            }
        }
    }
}