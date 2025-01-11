package com.example.csci4176project.views

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.csci4176project.adapters.GroupsAdapter
import com.example.csci4176project.R
import com.example.csci4176project.components.CurrencyConverter
import com.example.csci4176project.components.CurrencyUpdater
import com.example.csci4176project.models.GroupModel
import com.example.csci4176project.repositories.ExpenseRepository
import com.example.csci4176project.repositories.GroupRepository
import com.example.csci4176project.repositories.UserRepository
import com.example.csci4176project.util.CurrentUser

class GroupsActivity : AppCompatActivity() {

    private lateinit var addGroupBtn: Button
    private lateinit var groupList: RecyclerView
    private lateinit var adapter: GroupsAdapter
    private lateinit var owedText: TextView
    private lateinit var oweText: TextView
    private lateinit var currencyUpdater: CurrencyUpdater
    private val PREFS_NAME = "MyPrefs"
    private val CURRENCY_UPDATE_STATUS = "CurrencyUpdateStatus"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        owedText = findViewById(R.id.owedText)
        oweText = findViewById(R.id.oweText)
        groupList = findViewById(R.id.groupsList)

        addGroupBtn = findViewById(R.id.newGroupButton)
        addGroupBtn.setOnClickListener{
            navigateGroupsCreate()
        }

        populateViews()

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedToggleState = sharedPreferences.getBoolean("toggle_state_" + CurrentUser.userId, false)

        if (savedToggleState) {
            currencyUpdater = CurrencyUpdater(this)
            currencyUpdater.initialize(){}
        }

        Handler().postDelayed({
            handleAdapter()
        }, 1000)
        
    }

    // must include
    // synchronize get the permission request info, interact with currency updater
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        currencyUpdater.onRequestPermissionsResult(requestCode, grantResults)
    }
    //end

    /**
     * Function used to set the amounts the user owes and is owed in the corresponding text boxes
     */
    private fun setAmounts(){
        UserRepository.read(CurrentUser.userId!!){user->
            oweText.setText("0.00 ${user?.currency}")
            owedText.setText("0.00 ${user?.currency}")

            ExpenseRepository.getAllUserTransactions(CurrentUser.userId!!){expenseList->
                ExpenseRepository.getUserOwes(CurrentUser.userId!!, expenseList){owes->
                    val formatted = String.format("%.2f", CurrencyConverter.convert(owes, "USD", user?.currency!!))
                    oweText.setText("$formatted ${user?.currency}")
                }
                ExpenseRepository.getUserOwed(CurrentUser.userId!!, expenseList){owed->
                    val formatted = String.format("%.2f", CurrencyConverter.convert(owed, "USD", user?.currency!!))
                    owedText.setText("$formatted ${user?.currency!!}")
                }
            }
        }
    }

    // function used to set existing views
    private fun populateViews(){
        setAmounts()
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

        val profileBtn: ImageButton = findViewById(R.id.profileIcon)
        profileBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }


    // set transactions in the adapter for the recycler
    private fun handleAdapter(){
        val layoutManager = LinearLayoutManager(this)
        groupList.layoutManager = layoutManager
        adapter = GroupsAdapter()
        groupList.adapter = adapter

        // grab list of groups from user, then grab details for each group to display
        UserRepository.getUsersGroups(CurrentUser.userId!!) { groupIds ->

            val groupList = mutableListOf<GroupModel>()
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

    // function used to go to the create group activity
    private fun navigateGroupsCreate() {
        val createIntent = Intent(this, CreateGroupActivity::class.java)
        startActivity(createIntent)
    }
}