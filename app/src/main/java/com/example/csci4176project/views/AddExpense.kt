package com.example.csci4176project.views

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.csci4176project.R
import com.example.csci4176project.adapters.SelectUserAdapter
import com.example.csci4176project.components.CurrencyConverter
import com.example.csci4176project.models.UserModel
import com.example.csci4176project.repositories.ExpenseRepository
import com.example.csci4176project.repositories.GroupRepository
import com.example.csci4176project.repositories.UserRepository
import com.example.csci4176project.util.CurrentUser

class AddExpense : AppCompatActivity() {

    // Initialize variables
    private lateinit var discardExpBtn: ImageButton
    private lateinit var submitBtn: Button
    private lateinit var expenseName: EditText
    private lateinit var description: EditText
    private lateinit var amount: EditText
    private lateinit var type: EditText
    private lateinit var selectedUsers: RecyclerView
    private lateinit var adapter: SelectUserAdapter
    private lateinit var groupId: String
    private lateinit var edit: String
    private lateinit var splitGroup: RadioGroup
    private lateinit var evenSplitBtn: RadioButton
    private lateinit var exactSplitBtn: RadioButton
    private lateinit var percentInputContainer: LinearLayout
    private var splitMethod = SplitMethod.EVEN

    // Enum class for split method
    enum class SplitMethod {
        EVEN, EXACT
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        submitBtn = findViewById(R.id.submitExpense)
        expenseName = findViewById(R.id.expenseName)
        description = findViewById(R.id.description)
        amount = findViewById(R.id.amountInput)
        type = findViewById(R.id.type)
        selectedUsers = findViewById(R.id.memberSelect)
        groupId = intent.getStringExtra("groupId").toString()
        edit = intent.getStringExtra("edit").toString()

        // Initialize the split option buttons and percentage input container
        percentInputContainer = findViewById(R.id.percentageInputsContainer)
        splitGroup = findViewById<RadioGroup>(R.id.splitGroup)
        evenSplitBtn = findViewById<RadioButton>(R.id.evenSplitBtn)
        exactSplitBtn = findViewById<RadioButton>(R.id.exactSplitBtn)


        // populate the select members adapter
        handleAdapter()

        // if this activity is opened in edit mode, should already have existing information
        if(edit.isNotBlank() && edit != "null"){
            Log.d("test", edit)
            populateExistingViews()
        }

        discardExpBtn = findViewById(R.id.discardExp)
        discardExpBtn.setOnClickListener {
            finish()
        }

        evenSplitBtn.setOnClickListener{
            splitMethod = SplitMethod.EVEN
            percentInputContainer.removeAllViews()
            percentInputContainer.visibility = View.GONE
        }

        exactSplitBtn.setOnClickListener{
            splitMethod = SplitMethod.EXACT
            percentInputContainer.visibility = View.VISIBLE
            percentInputContainer.removeAllViews()
            // Add EditText for current user
            UserRepository.read(CurrentUser.userId!!) { currentUser ->
                if (currentUser != null) {
                    val currentUserEditText = createPercentInput("your share", CurrentUser.userId!!)
                    percentInputContainer.addView(currentUserEditText)
                }
            }
            // Logic to add EditText for other selected users
            val selectedUserIds = adapter.getSelectedUsers()
            selectedUserIds.forEach { userId ->
                val index = adapter.userIds.indexOf(userId)
                if (index != -1) {
                    val user = adapter.userList[index]
                    val editText = createPercentInput(user.name ?: "Unknown user", userId)
                    percentInputContainer.addView(editText)
                }
            }
        }

        submitBtn.setOnClickListener{
            val selectedRadioButtonId = splitGroup.checkedRadioButtonId
            if (selectedRadioButtonId != -1) {
                if(validateExpense()){
                    val amountValue = amount.text.toString().toDouble()
                    val converted = CurrencyConverter.convert(amountValue, CurrentUser.currency!!, "USD")
                    val participantList = adapter.getSelectedUsers().toMutableList().apply { add(CurrentUser.userId!!) }
                    // Calculate the amount each user owes based on the split method
                    val userOweAmount = if (splitMethod == SplitMethod.EXACT) {
                        calculateSplit(converted, participantList, SplitMethod.EXACT, getSplitInputs())
                    } else {
                        calculateSplit(converted, participantList, SplitMethod.EVEN)
                    }

                if(edit.isNotBlank() && edit != "null"){
                    ExpenseRepository.update(groupId, edit,
                        expenseName.text.toString(), description.text.toString(), type.text.toString(),
                        converted.toString().toDouble(), CurrentUser.userId!!, false, userOweAmount)
                }
                else{
                    ExpenseRepository.create(groupId,
                        expenseName.text.toString(), description.text.toString(), type.text.toString(),
                        converted.toString().toDouble(), CurrentUser.userId!!, userOweAmount)
                }

                    navigateGroups()
                }
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Function used to grab existing information about an expense and showcase it when the user
     * decides to edit
     */
    private fun populateExistingViews(){
        val header: TextView = findViewById(R.id.heading)
        header.text = "Edit Expense"
        ExpenseRepository.read(groupId, edit){expense->
            expenseName.setText(expense?.expenseName)
            description.setText(expense?.description)
            type.setText(expense?.type)

            val converted = CurrencyConverter.convert(expense?.amount!!, "USD", CurrentUser.currency!!)
            amount.setText(converted.toString())

            val selectedUsers = mutableSetOf<String>()

            // select users who were already involved in the expense
            expense?.participants?.keys?.forEach{
                selectedUsers.add(it)
            }

            adapter.setSelectedUsers(selectedUsers)
        }
    }

    /**
     * Function used to calculate the amounts owed by each user
     */
    private fun calculateSplit(amount: Double, participantIds: List<String>, splitMethod: SplitMethod, exactSplitPercentages: Map<String, Double> = emptyMap()): Map<String, Double> {
        val sharesMap = mutableMapOf<String, Double>()
        // Calculate based on split method
        when (splitMethod) {
            SplitMethod.EVEN -> {
                val share = amount / participantIds.size
                participantIds.forEach { participantId ->
                    sharesMap[participantId] = share
                }
            }
            SplitMethod.EXACT -> {
                exactSplitPercentages.forEach { (participantId, percentage) ->
                    val share = amount * (percentage / 100)
                    sharesMap[participantId] = share
                }
            }
        }
        return sharesMap.filter { it.key != CurrentUser.userId!! }
    }

    /**
     * Function used to swap to the group info activity
     */
    fun navigateGroups() {
        finish()
    }

    /**
     * Function used to set the adapter used by the recycler view and display members accordingly
     */
    private fun handleAdapter(){
        val layoutManager = LinearLayoutManager(this)
        selectedUsers.layoutManager = layoutManager
        adapter = SelectUserAdapter()
        selectedUsers.adapter = adapter

        GroupRepository.getGroupMembers(groupId) { userIds ->
            val updatedIds = userIds.filter { it != CurrentUser.userId }
            val userList = mutableListOf<UserModel>()

            // for every user in the group, show them in the result list
            for (id in updatedIds) {
                UserRepository.read(id) { user ->
                    if (user != null && id != CurrentUser.userId) {
                        userList.add(user)
                        adapter.setUserList(userList, updatedIds)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    // Helper function to get percentage inputs
    private fun getSplitInputs(): Map<String, Double> {
        val inputs = mutableMapOf<String, Double>()
        for (i in 0 until percentInputContainer.childCount) {
            val editText = percentInputContainer.getChildAt(i) as EditText
            val userId = editText.tag.toString()
            val percentage = editText.text.toString().toDoubleOrNull() ?: 0.0
            inputs[userId] = percentage
        }
        return inputs
    }

    // Helper function to create EditText for percentage input
    private fun createPercentInput(hintName: String, tag: String): EditText {
        return EditText(this@AddExpense).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            hint = "Enter percentage for $hintName"
            inputType = InputType.TYPE_CLASS_NUMBER
            this.tag = tag
        }
    }

    /**
     * Utility function used to validate if expense details are filled out properly
     */
    private fun validateExpense():Boolean {
        var msg = ""
        if (expenseName.text.isEmpty()) {
            msg = "Please name expense!"
        }
        else if (description.text.isEmpty()) {
            msg = "Please add description!"
        }
        else if (amount.text.isEmpty()) {
            msg = "Please add amount!"
        }
        else if (adapter.getSelectedUsers().isEmpty()) {
            msg = "Must include at least one member!"
        }
        // Additional validation for exact split
        if (splitMethod == SplitMethod.EXACT) {
            val percentages = getSplitInputs()
            val totalPercentage = percentages.values.sum()
            if (totalPercentage != 100.0) {
                Toast.makeText(this, "Percentages must sum to 100", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        if (msg.isEmpty()) {
            return true
        } else {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            return false
        }
    }


}
