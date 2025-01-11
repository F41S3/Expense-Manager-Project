package com.example.csci4176project.views

import com.example.csci4176project.adapters.NotificationAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.csci4176project.R
import com.example.csci4176project.models.GroupModel
import com.example.csci4176project.repositories.GroupRepository
import com.example.csci4176project.repositories.UserRepository
import com.example.csci4176project.util.CurrentUser

open class NotificationsActivity : AppCompatActivity() {

    private lateinit var inviteList: RecyclerView
    private lateinit var adapter: NotificationAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        inviteList = findViewById(R.id.notificationRecyclerView)
        inviteList.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter()
        inviteList.adapter = adapter

        // set up existing views
        populateViews()

        Handler().postDelayed({
            handleAdapter()
        }, 1000)
    }

    // function used to set up the adapter for the list of notifications
    fun handleAdapter(){
        UserRepository.getUsersInvites(CurrentUser.userId!!){groupIds->
            val groupList = mutableListOf<GroupModel>()

            // for every invite in the user's invite list, display information about the group that
            // they were invited to
            for (id in groupIds) {
                GroupRepository.read(id) { group ->
                    if (group != null) {
                        groupList.add(group)
                        adapter.setGroupList(groupList, groupIds)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    // function used to set existing views
    fun populateViews(){

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
    }
}