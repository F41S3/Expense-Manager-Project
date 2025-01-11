package com.example.csci4176project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.csci4176project.R
import com.example.csci4176project.components.CurrencyConverter
import com.example.csci4176project.models.ExpenseModel
import com.example.csci4176project.util.CurrentUser
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Adapter used to display the list of overall transactions a user is partaking in
 */
class TransactionAdapter(var expenseList: List<ExpenseModel> = emptyList()) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.bind(expense)
    }

    override fun getItemCount(): Int = expenseList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val expenseName = itemView.findViewById<TextView>(R.id.transactionGroup)
        val expenseDesc = itemView.findViewById<TextView>(R.id.transactionDescription)
        val expAmount = itemView.findViewById<TextView>(R.id.amountText)
        val expDate = itemView.findViewById<TextView>(R.id.transactionDate)

        // for each expense, display the name, description, and amount
        fun bind(exp: ExpenseModel) {
            expenseName.setText(exp.expenseName)
            expenseDesc.setText(exp.description)

            // format the amount to two decimal places
            val formattedAmount = String.format("%.2f", CurrencyConverter.convert(exp.amount!!, "USD", CurrentUser.currency!!))
            expAmount.setText(formattedAmount)

            val date = Date(exp.timestamp)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy")

            expDate.setText(dateFormat.format(date))
        }

    }

    fun setExpenses(expList: List<ExpenseModel>) {
        this.expenseList = expList
    }

}