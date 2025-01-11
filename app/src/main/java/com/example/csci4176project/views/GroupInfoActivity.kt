package com.example.csci4176project.views

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.content.Intent
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.csci4176project.R
import com.example.csci4176project.adapters.GroupTransactionAdapter
import com.example.csci4176project.components.CurrencyConverter
import com.example.csci4176project.models.ExpenseModel
import com.example.csci4176project.repositories.ExpenseRepository
import com.example.csci4176project.repositories.GroupRepository
import com.example.csci4176project.util.CurrentUser


class GroupInfoActivity : ComponentActivity() {

    private lateinit var groupName : TextView
    private lateinit var groupId: String
    private lateinit var ownTransList: RecyclerView
    private lateinit var adapter: GroupTransactionAdapter
    private lateinit var oweAmount: TextView
    private lateinit var owedAmount: TextView
    private lateinit var deleteBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_info)

        groupName = findViewById(R.id.groupName)
        oweAmount = findViewById(R.id.oweAmount)
        owedAmount = findViewById(R.id.owedAmount)
        deleteBtn = findViewById(R.id.deleteBtn)

        // grab information from previous activity
        groupId = intent.getStringExtra("groupId").toString()
        ownTransList= findViewById(R.id.ownTrasnactionList)
        populateView()

        // actively update values to showcase correct value
        updateAdapterInRuntime()
    }

    /**
     * Function used to set the amounts the user owes and is owed in the corresponding text boxes
     */
    private fun setAmounts(){

        // use utility functions from repository and display information
        ExpenseRepository.readExpensesForUserInGroup(CurrentUser.userId!!, groupId){expenseList->
            ExpenseRepository.getUserOwes(CurrentUser.userId!!, expenseList){owes->
                val formatted = String.format("%.2f", CurrencyConverter.convert(owes, "USD", CurrentUser.currency!!))
                oweAmount.setText("$formatted ${CurrentUser.currency}")
            }
        }

        ExpenseRepository.readExpensesForUserInGroup(CurrentUser.userId!!, groupId){expenseList->
            ExpenseRepository.getUserOwed(CurrentUser.userId!!, expenseList){owed->
                val formatted = String.format("%.2f", CurrencyConverter.convert(owed, "USD", CurrentUser.currency!!))
                owedAmount.setText("$formatted ${CurrentUser.currency}")
            }
        }
    }


    // set transactions in the adapter for the recycler
    private fun handleAdapter(){
        val layoutManager = LinearLayoutManager(this)
        ownTransList.layoutManager = layoutManager
        adapter = GroupTransactionAdapter()
        ownTransList.adapter = adapter

        // grab all the expenses for a user in a group, then read details for each one
        ExpenseRepository.readExpenseIDsForUserInGroup(CurrentUser.userId!!, groupId) { expenseIds ->
            val expenses = mutableListOf<ExpenseModel>()
            for(id in expenseIds){
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

    // function used to set up existing views
    fun populateView(){
        setAmounts()
        GroupRepository.read(groupId) { group ->
            if (group != null) {
                groupName.setText(group.name)
            }
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
            intent.putExtra("groupId", groupId)
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

        val viewMembers: Button = findViewById(R.id.viewMembersBtn)
        viewMembers.setOnClickListener {
            val intent = Intent(this, MemListActivity::class.java).apply {
                putExtra("groupId", groupId)
            }
            startActivity(intent)
        }

        val addExpense: Button = findViewById(R.id.addExpenseBtn)
        addExpense.setOnClickListener {
            val intent = Intent(this, AddExpense::class.java).apply {
                putExtra("groupId", groupId)
            }
            startActivity(intent)
        }

        val backBtn: ImageButton = findViewById(R.id.goBackIcon)
        backBtn.setOnClickListener {
            val intent = Intent(this, GroupsActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        val transactionBtn: Button = findViewById(R.id.allTransactionsBtn)
        transactionBtn.setOnClickListener {
            val intent = Intent(this, AllGroupTransactions::class.java).apply {
                putExtra("groupId", groupId)
            }
            intent.putExtra("groupId", groupId)
            startActivity(intent)
        }

        deleteBtn.setOnClickListener{
            showConfirmationDialog()
        }
    }

    // helper function used to give user chance to confirm deletion of a group
    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Deletion")
        builder.setMessage("Are you sure you want to delete this group?")
        builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
            deleteGroup()
        }
        builder.setNegativeButton("No") { dialogInterface: DialogInterface, i: Int ->

        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    // function calls on group repo to delete the group
    private fun deleteGroup() {
        GroupRepository.delete(groupId)
        val intent = Intent(this, GroupsActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    private fun updateAdapterInRuntime(){
        ExpenseRepository.reactToExpenseListChanges(groupId, {setAmounts()}, {handleAdapter()})
    }
}