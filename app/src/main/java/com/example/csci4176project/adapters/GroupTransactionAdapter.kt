package com.example.csci4176project.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.csci4176project.R
import com.example.csci4176project.components.CurrencyConverter
import com.example.csci4176project.models.ExpenseModel
import com.example.csci4176project.repositories.UserRepository
import com.example.csci4176project.util.CurrentUser
import com.example.csci4176project.repositories.ExpenseRepository
import com.example.csci4176project.views.ExpenseInfoActivity
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Adapter used to showcase transactions within a group
 */
class GroupTransactionAdapter(private var expenseList: List<ExpenseModel> = emptyList(), var expenseIds: List<String> = emptyList()) : RecyclerView.Adapter<GroupTransactionAdapter.ViewHolder>() {

    private lateinit var context: Context
    private lateinit var groupId: String
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        groupId = (parent.context as Activity).intent.getStringExtra("groupId").toString()
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.group_transaction_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenseList[position]

        // use an on click to navigate to more information on a particular expense
        holder.itemView.setOnClickListener{
            val expenseId = expenseIds[position]
            val intent = Intent(context, ExpenseInfoActivity::class.java).apply {
                putExtra("groupId", groupId)
                putExtra("expenseId", expenseId)
            }
            context.startActivity(intent)
        }

        ExpenseRepository.read(groupId, expenseIds[position]){expense ->
            if (expense != null) {
                if(expense.expensePaidOff){
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
                }
            }
        }

        CurrentUser.userId?.let { holder.bind(expense, it, context) }
    }

    override fun getItemCount(): Int = expenseList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rootLayout: LinearLayout = itemView.findViewById(R.id.group_transaction_item)
        val expenseName = itemView.findViewById<TextView>(R.id.titleTextView)
        val expenseDesc = itemView.findViewById<TextView>(R.id.descriptionTextView)
        val expAmount = itemView.findViewById<TextView>(R.id.amountTextView)
        val expDate = itemView.findViewById<TextView>(R.id.dateTextView)

        fun bind(exp: ExpenseModel, currentUserID: String, context: Context) {
            expenseName.setText(exp.expenseName)

            exp.payerId?.let {
                UserRepository.read(it) { user ->
                    if (user != null) {
                        expenseDesc.setText(user.name.toString() + " added this expense.")
                    }
                }
            }

            val formatted = String.format("%.2f", CurrencyConverter.convert(exp.amount!!, "USD", CurrentUser.currency!!))
            expAmount.setText("$formatted ${CurrentUser.currency}")

            val currentUserPaid = exp.payerId == currentUserID

            if (!currentUserPaid) {
                rootLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.lightred))
            } else {
                rootLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.lightgreen))
            }

            // use dateformatter to convert the long time stamp into a date
            val date = Date(exp.timestamp)
            val dateFormat = SimpleDateFormat("MM/dd/yyyy")
            expDate.setText(dateFormat.format(date))
        }
    }

    fun setExpenses(expList: List<ExpenseModel>, expIds: List<String>) {
        this.expenseList = expList
        this.expenseIds = expIds
//        notifyDataSetChanged()
    }
}