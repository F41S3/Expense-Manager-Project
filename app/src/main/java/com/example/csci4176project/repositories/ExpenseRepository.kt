package com.example.csci4176project.repositories

import android.util.Log
import com.example.csci4176project.models.ExpenseModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.atomic.AtomicInteger

class ExpenseRepository {

    // create a static class that acts as a DB helper in relation to expenses
    companion object{
        private val db : DatabaseReference by lazy {
            FirebaseDatabase.getInstance().reference
        }

        /**
         * Function used to create an expense in the database. Has to create the expense under the exepense
         * db and add the reference to each user involved
         */
        fun create(groupId: String, expenseName: String, description: String, type: String,
                   amount: Double, payerId: String, participants: Map<String, Double>){

            val expensesRef = db.child("expenses").child(groupId)
            val expenseId = expensesRef.push().key

            val expense = ExpenseModel(expenseName, description, type, amount, payerId, false, participants)

            // create expense in expense db
            if(expenseId != null){
                expensesRef.child(expenseId).setValue(expense)
            }

            // link expense to each user involved
            UserRepository.addExpenseToGroup(payerId, groupId, expenseId!!)

            participants.forEach { (uid, _) ->
                UserRepository.addExpenseToGroup(uid, groupId, expenseId!!)
            }
        }

        /**
         * Function used to read details about a particular expense based on group and expense id
         */
        fun read(groupId: String, expenseId: String, callback: (ExpenseModel?) -> Unit) {
            val expenseRef = db.child("expenses").child(groupId).child(expenseId)

            // find the corresponding expense under the corresponding group and return it to be used by the callback
            expenseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expense = snapshot.getValue(ExpenseModel::class.java)
                    callback(expense)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR", error.toString())
                }
            })
        }

        /**
         * Function used to delete an expense from the database. deletes the reference from every user
         * involved as well
         */
        fun delete(groupId: String, expenseId: String){
            val expensesRef = db.child("expenses").child(groupId).child(expenseId)

            // for every user involved in the expense, remove the expense from their reference
            read(groupId, expenseId){expense->
                if (expense != null) {
                    expense.participants.forEach{(userId, _)->
                        UserRepository.removeExpense(userId, groupId, expenseId)
                    }
                    UserRepository.removeExpense(expense.payerId!!, groupId, expenseId)
                }

                // remove the expense from the expense db at the very end to avoid null reference errors
                expensesRef.removeValue()
            }
        }

        /**
         * Util function used to delete all expenses under a particular group (used by delete group)
         */
        fun deleteGroup(groupId: String){
            val expensesRef = db.child("expenses").child(groupId)
            expensesRef.removeValue()
        }

        /**
         * Function used to update a particular expense. A user can pass in whatever values they want to update
         * and only those values will be updated
         */
        fun update(groupId: String, expenseId: String, expenseName: String? = null, description: String? = null,
                   type: String? = null, amount: Double? = null, payerId: String? = null, expensePaidOff: Boolean = false,
                   participants: Map<String, Double>) {

            val expenseUpdates = HashMap<String, Any>()

            // checks which values have been supplied to see what to update
            expenseName?.let { expenseUpdates["expenseName"] = it }
            description?.let { expenseUpdates["description"] = it }
            type?.let { expenseUpdates["type"] = it }
            amount?.let { expenseUpdates["amount"] = it }
            payerId?.let { expenseUpdates["payerId"] = it }
            expensePaidOff?.let { expenseUpdates["expensePaidOff"] = it }
            participants?.let { expenseUpdates["participants"] = it }

            // update participant list accordingly
            read(groupId, expenseId) { expense ->
                expense?.let { exp ->

                    // if there are users from the old list that aren't in the new one, remove their expense
                    exp.participants.keys.forEach { userId ->
                        if (userId !in participants) {
                            UserRepository.removeExpense(userId, groupId, expenseId)
                        }
                    }

                    // if there are users from the new list that aren't in the old one, add an expense reference in them
                    participants.keys.forEach { userId ->
                        if (userId !in exp.participants) {
                            UserRepository.addExpenseToGroup(userId, groupId, expenseId)
                        }
                    }

                    // finally update the expense with the new values
                    val expenseRef = db.child("expenses").child(groupId).child(expenseId)
                    expenseRef.updateChildren(expenseUpdates)
                }
            }
        }

        /**
         * Function used to read all the expenses in a particular group
         */
        fun readExpensesForGroup(groupId: String, callback: (List<ExpenseModel>) -> Unit) {
            val expensesRef = db.child("expenses").child(groupId)
            val expensesList = mutableListOf<ExpenseModel>()

            // for every expense under a particular groupsID, add it to the list to return in the callback
            expensesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (expenseSnapshot in snapshot.children) {
                        val expense = expenseSnapshot.getValue(ExpenseModel::class.java)
                        expense?.let {
                            expensesList.add(it)
                        }
                    }
                    callback(expensesList)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled
                }
            })
        }

        /**
         * Function used to for a particular user, read the expenses they are involved in, regarding
         * a specific group
         */
        fun readExpensesForUserInGroup(userId: String, groupId: String, callback: (List<ExpenseModel>) -> Unit) {
            val expensesRef = db.child("expenses").child(groupId)
            val expensesList = mutableListOf<ExpenseModel>()

            expensesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // for every expense under the respective groupId for a user, add it to the list
                    for (expenseSnapshot in snapshot.children) {
                        val expense = expenseSnapshot.getValue(ExpenseModel::class.java)
                        expense?.let {
                            // Check if the user is a participant in the expense
                            if (userId in it.participants.keys || userId == it.payerId) {
                                expensesList.add(it)
                            }
                        }
                    }
                    callback(expensesList)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled
                }
            })
        }

        /**
         * Function used to get a list of expenseIds for all the expenses involving a user pertaining
         * a specific group
         */
        fun readExpenseIDsForUserInGroup(userId: String, groupId: String, callback: (List<String>) -> Unit) {
            val expensesRef = db.child("expenses").child(groupId)
            val expensesList = mutableListOf<String>()

            expensesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (expenseSnapshot in snapshot.children) {
                        val expense = expenseSnapshot.getValue(ExpenseModel::class.java)
                        expense?.let {
                            // Check if the user is a participant in the expense
                            if (userId in it.participants.keys || userId == it.payerId) {
                                expensesList.add(expenseSnapshot.key.toString())
                            }
                        }
                    }
                    callback(expensesList)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled
                }
            })
        }

        /**
         * Function used to get a list of expenseIds for all the expenses pertaining
         * a specific group
         */
        fun readExpenseIDsForGroup(groupId: String, callback: (List<String>) -> Unit) {
            val expensesRef = db.child("expenses").child(groupId)
            val expensesList = mutableListOf<String>()

            expensesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (expenseSnapshot in snapshot.children) {

                        // every expense under a particular groupId should be added to the list to return
                        val expense = expenseSnapshot.getValue(ExpenseModel::class.java)
                        expense?.let {
                            expensesList.add(expenseSnapshot.key.toString())
                        }
                    }
                    callback(expensesList)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled
                }
            })
        }

        /**
         * Function used to fetch every user transaction made, either owing or owed across all groups
         */
        fun getAllUserTransactions(userId: String, callback: (List<ExpenseModel>) -> Unit) {
            UserRepository.getUsersGroups(userId) { groupIds ->
                val allExpenses = mutableListOf<ExpenseModel>()
                val groupExpensesCount = AtomicInteger(groupIds.size)

                // Callback function to aggregate expenses and invoke the main callback when all groups are processed
                val aggregateExpenses: (List<ExpenseModel>) -> Unit = { expenses ->
                    allExpenses.addAll(expenses)
                    if (groupExpensesCount.decrementAndGet() == 0) {
                        callback(allExpenses)
                    }
                }

                if(groupIds.isEmpty()){
                    callback(emptyList())
                }
                else{
                    // Retrieve expenses for each group
                    groupIds.forEach { groupId ->
                        readExpensesForUserInGroup(userId, groupId) { expenses ->
                            aggregateExpenses(expenses)
                        }
                    }
                }
            }
        }

        /**
         * Utility function used to calculate the total amount a user owes
         */
        fun getUserOwes(userId: String, expenses: List<ExpenseModel>, callback: (Double) -> Unit) {
            var totalOwes = 0.0
            expenses.forEach { expense ->
                // for every transaction a user is involved in, if they didn't pay, then they owe money
                if (expense.payerId != userId) {
                    val amountPaid = expense.participants[userId] ?: 0.0
                    totalOwes += amountPaid
                }
            }
            callback(totalOwes)
        }

        /**
         * Utility function used to calculate the total amount a user is owed
         */
        fun getUserOwed(userId: String, expenses: List<ExpenseModel>, callback: (Double) -> Unit) {
            var totalOwed = 0.0
            expenses.forEach { expense ->
                // for every transaction a user is involved in, if they paid, then they are owed money
                if (expense.payerId == userId) {
                    expense.participants.forEach{participantId, amountPaid ->
                        totalOwed += amountPaid
                    }
                }
            }
            callback(totalOwed)
        }

        fun reactToExpenseListChanges(groupId: String, vararg methods: () -> Unit){
            val ref = db.ref.child("expenses").child(groupId)

            ref.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (method in methods){
                        method()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Database Error", error.message)
                }

            })
        }
    }
}