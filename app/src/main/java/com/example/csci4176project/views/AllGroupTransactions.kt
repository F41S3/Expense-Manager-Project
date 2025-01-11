package com.example.csci4176project.views

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.csci4176project.R
import com.example.csci4176project.repositories.ExpenseRepository
import com.example.csci4176project.adapters.GroupTransactionAdapter
import com.example.csci4176project.models.ExpenseModel
import com.example.csci4176project.repositories.GroupRepository

class AllGroupTransactions : ComponentActivity() {
    private lateinit var cancelBtn: ImageButton
    private lateinit var transList: RecyclerView
    private lateinit var adapter: GroupTransactionAdapter
    private lateinit var groupId: String
    private lateinit var groupName: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_group_transactions)
        cancelBtn = findViewById(R.id.discardGroup)
        groupName = findViewById(R.id.textView)
        transList = findViewById(R.id.transactionList)
        groupId = intent.getStringExtra("groupId").toString()

        // set up views
        populateView()

        // set up adapter
        updateAdapterInRuntime()
    }

    /**
     * Function used to set the adapter for the recycler with the correct transactions for the
     * group
     */
    private fun handleAdapter(){
        val layoutManager = LinearLayoutManager(this)
        transList.layoutManager = layoutManager
        adapter = GroupTransactionAdapter()
        transList.adapter = adapter

        ExpenseRepository.readExpenseIDsForGroup(groupId) { expenseIds ->

            // first get every expense that corresponds to the group
            val expenses = mutableListOf<ExpenseModel>()
            for(id in expenseIds){

                // for every expense in the group, fetch the expense object and add to the list to display
                ExpenseRepository.read(groupId, id){expense ->
                    if(expense != null){
                        expenses.add(expense)
                        adapter.setExpenses(expenses, expenseIds)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * Function used to set up the view objects on screen
     */
    fun populateView(){
        cancelBtn.setOnClickListener {
            finish()
        }

        GroupRepository.read(groupId){group->
            groupName.setText(group?.name + " Transaction Log")
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
    }

    fun updateAdapterInRuntime(){
        ExpenseRepository.reactToExpenseListChanges(groupId, { handleAdapter() })
    }
}