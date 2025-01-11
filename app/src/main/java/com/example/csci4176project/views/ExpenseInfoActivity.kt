package com.example.csci4176project.views

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.csci4176project.R
import com.example.csci4176project.adapters.PayerAdapter
import com.example.csci4176project.components.CurrencyConverter
import com.example.csci4176project.repositories.ExpenseRepository
import com.example.csci4176project.repositories.UserRepository
import com.example.csci4176project.util.CurrentUser
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date

class ExpenseInfoActivity : AppCompatActivity() {

    private lateinit var backBtn: ImageButton
    private lateinit var payBtn: Button
    private lateinit var deleteBtn: ImageButton
    private lateinit var editBtn: ImageButton

    private lateinit var expenseName: TextView
    private lateinit var expenseDesc: TextView
    private lateinit var expenseType: TextView
    private lateinit var payer: TextView
    private lateinit var pfp : ImageView
    private lateinit var expenseAmount: TextView
    private lateinit var expenseDate: TextView

    private lateinit var payList: RecyclerView
    private lateinit var adapter: PayerAdapter

    private lateinit var groupId: String
    private lateinit var expenseId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_info)

        backBtn = findViewById(R.id.backBtn)
        payBtn = findViewById(R.id.payBtn)
        deleteBtn = findViewById(R.id.deleteBtn)
        editBtn = findViewById(R.id.editBtn)

        expenseName = findViewById(R.id.expenseNameText)
        expenseDesc = findViewById(R.id.descriptionText)
        expenseType = findViewById(R.id.typeText)
        expenseAmount = findViewById(R.id.amountText)
        expenseDate = findViewById(R.id.dateText)
        payer = findViewById(R.id.payerText)
        pfp = findViewById(R.id.imageView2)
        payList = findViewById(R.id.payerList)

        // grab passed in information from previous activity
        groupId = intent.getStringExtra("groupId").toString()
        expenseId = intent.getStringExtra("expenseId").toString()


        backBtn.setOnClickListener{
            finish()
        }

        deleteBtn.setOnClickListener{
            // if user wants to delete expense, confirm it before deleting
            showConfirmationDialog()
        }

        editBtn.setOnClickListener{
            // if user chooses to edit, start activity in edit mode
            val intent = Intent(this, AddExpense::class.java).apply {
                putExtra("groupId", groupId)
                putExtra("edit", expenseId)
            }
            startActivity(intent)
        }

        payBtn.setOnClickListener{
            handlePayment()
        }

        // set up views
        populateViews()

        // populate adapter list
        handleAdapter()
    }

    override fun onResume() {
        super.onResume()
        populateViews()
        handleAdapter()
    }

    /**
     * Function used to allow a user to mark an expense as paid
     */
    private fun handlePayment() {
        val bundle = intent.extras

        ExpenseRepository.read(bundle?.getString("groupId")!!, bundle.getString("expenseId")!!) { expense ->
            if (expense != null) {

                val currentUserID = CurrentUser.userId

                if (currentUserID != null) {
                    // update the map to change the amount owed for the current user to 0
                    val updatedParticipants = expense.participants.toMutableMap()

                    updatedParticipants[currentUserID] = 0.0
                    val isExpensePaidOffByAll = updatedParticipants.any { participant -> participant.value != 0.0 }

                    // pass updates into the expense repository
                    ExpenseRepository.update(
                        bundle.getString("groupId")!!,
                        bundle.getString("expenseId")!!,
                        expensePaidOff = !isExpensePaidOffByAll,
                        participants = updatedParticipants
                    )

                    finish()
                } else {
                    Log.e("ERROR:", "Current user ID is null!")
                }
            } else {
                Log.e("ERROR:", "Could not find expense!")
            }
        }
    }


    /**
     * Function used to confirm a user's desire to delete an expense
     */
    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Deletion")
        builder.setMessage("Are you sure you want to delete this expense?")
        builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
            deleteExpense()
        }
        builder.setNegativeButton("No") { dialogInterface: DialogInterface, i: Int ->

        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    // helper function to call on delete expense method from the repositories
    private fun deleteExpense() {
        Log.d("test", groupId)
        Log.d("test", expenseId)
        ExpenseRepository.delete(groupId, expenseId)
        finish()
    }

    // helper function to set up views with existing information
    fun populateViews(){
        ExpenseRepository.read(groupId, expenseId){expense ->
            expenseName.setText(expense?.expenseName)
            expenseDesc.setText("Description: " + expense?.description)
            expenseType.setText("Type: " + expense?.type)

            val formatted = String.format("%.2f", CurrencyConverter.convert(expense?.amount!!, "USD", CurrentUser.currency!!))
            expenseAmount.setText("$formatted ${CurrentUser.currency}")
            
            val date = expense?.let { Date(it.timestamp) }
            val dateFormat = SimpleDateFormat("dd/MM/yyyy")
            expenseDate.setText(dateFormat.format(date))

            if (expense != null) {
                expense.payerId?.let {
                    UserRepository.read(it){ user->
                        if (user != null) {
                            payer.text = user.name
                            val id = user.pfp?.substringAfterLast("/")
                            val imageUrl = "https://firebasestorage.googleapis.com/v0/b/csci4176project-cbc40.appspot.com/o/$id?alt=media"
                            Picasso.get().load(imageUrl)
                                .error(R.drawable.default_profile_picture)
                                .fit()
                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                .into(pfp)
                        }
                    }
                }
            }

            // if the user created the expense, they can delete or edit it
            if (expense?.payerId == CurrentUser.userId) {
                deleteBtn.visibility = View.VISIBLE
                payBtn.visibility = View.GONE
                editBtn.visibility = View.VISIBLE
            } else {
                deleteBtn.visibility = View.GONE
                editBtn.visibility = View.GONE


                val currentUserIsParticipant = expense?.participants?.containsKey(CurrentUser.userId) ?: false

                // if the user created the expense OR paid, don't show the pay button as an option
                if (!currentUserIsParticipant) {
                    payBtn.visibility = View.GONE
                } else {
                    val currentUserPaid = expense?.participants?.get(CurrentUser.userId) == 0.0
                    payBtn.visibility = if (currentUserPaid) View.GONE else View.VISIBLE
                }
            }

        }
    }

    // set up transaction list adapter
    fun handleAdapter(){
        val layoutManager = LinearLayoutManager(this)
        payList.layoutManager = layoutManager
        adapter = PayerAdapter()
        payList.adapter = adapter

        ExpenseRepository.read(groupId, expenseId){expense->

            // add every user involved as a payer into the list
            val participants = expense?.participants
            if (participants != null) {
                adapter.setParticipants(participants)
                adapter.notifyDataSetChanged()
            }
        }
    }
}