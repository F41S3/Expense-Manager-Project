package com.example.csci4176project.views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.csci4176project.R
import com.example.csci4176project.adapters.TransactionAdapter
import com.example.csci4176project.repositories.ExpenseRepository
import com.example.csci4176project.util.CurrentUser

class TransactionActivity : AppCompatActivity() {

    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private var filterToggle: Boolean = false
    private lateinit var groupId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)
        groupId = intent.getStringExtra("groupId").toString()

        transactionRecyclerView = findViewById(R.id.transactionRecyclerView)

        // depending if the filter is toggled, display either all transactions, or transactions in which you owe money
        var filterButton : Button = findViewById(R.id.filterExpenses)
        filterButton.setOnClickListener {

            if (filterToggle) {
                filterToggle = false
                filterButton.setBackgroundColor(getResources().getColor(R.color.black))
                filterButton.setText("Your transactions")
            }
            else {
                filterToggle = true
                filterButton.setBackgroundColor(getResources().getColor(R.color.buttonBackground))
                filterButton.setText("All transactions")
            }
            handleAdapter()
        }

        // set up views
        populateViews()
        handleAdapter()
    }

    // function used to set up the adapter with all the transactions a user is involved
    private fun handleAdapter(){
        val layoutManager = LinearLayoutManager(this)
        transactionRecyclerView.layoutManager = layoutManager
        adapter = TransactionAdapter()
        transactionRecyclerView.adapter = adapter

        // grab every transaction a user is involved in. depending on filter, display only the transactions where
        // the user owes money
        ExpenseRepository.getAllUserTransactions(CurrentUser.userId!!){expenses->
            if(!filterToggle){
                adapter.setExpenses(expenses)
            }
            else{
                val filteredExpenses = expenses.filter { it.payerId != CurrentUser.userId!! }
                adapter.setExpenses(filteredExpenses)
            }

            adapter.notifyDataSetChanged()
        }

    }

    // function used to set up views
    fun populateViews(){
        val notifBtn: ImageButton = findViewById(R.id.notificationIcon)
        notifBtn.setOnClickListener{
            val intent = Intent(this, NotificationsActivity::class.java)
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
