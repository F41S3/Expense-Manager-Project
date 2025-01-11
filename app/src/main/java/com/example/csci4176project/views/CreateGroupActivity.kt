package com.example.csci4176project.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.example.csci4176project.adapters.SelectUserAdapter
import com.example.csci4176project.util.CurrentUser
import com.example.csci4176project.R
import com.example.csci4176project.repositories.GroupRepository

class CreateGroupActivity : AppCompatActivity() {

    private lateinit var cancelBtn: ImageButton
    private lateinit var groupName: EditText
    private lateinit var addMembers: EditText
    private lateinit var createBtn: Button
    private lateinit var errorView: TextView
    private lateinit var spinner: Spinner
    lateinit var adapter: SelectUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        // initializing all views
        cancelBtn = findViewById(R.id.discardGroup)
        groupName = findViewById(R.id.editTextText)
        addMembers = findViewById(R.id.searchMemberText)
        createBtn = findViewById(R.id.submitGroup)
        errorView = findViewById(R.id.errorTextView)
        spinner = findViewById(R.id.spinner)

        // set up fragments
        val manager: FragmentManager = supportFragmentManager
        val memberSearch = MemberDisplayFragment()
        val frag = manager.beginTransaction()
        frag.replace(R.id.usersList, memberSearch).commit()

        adapter = memberSearch.getAdapter()


        memberSearch.handleAdapter()
        var timer: CountDownTimer? = null

        addMembers.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                memberSearch.loadProgressBar()
                memberSearch.listClear()
            }
            override fun afterTextChanged(text: Editable) {
                timer?.cancel()
                timer = object : CountDownTimer(1500, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        memberSearch.closeProgressBar()
                        memberSearch.searchProfiles(text.toString())
                    }
                }.start()
            }
        })

        cancelBtn.setOnClickListener {
            finish()
        }

        // setup spinner
        val spinnerOptions = arrayOf("Personal", "General", "Friends", "Work","Travel", "Food","Roommates",
            "Entertainment","Bills", "Shopping", "Miscellaneous")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = spinnerAdapter
        spinner.setSelection(spinnerOptions.indexOf("General"))

        createBtn.setOnClickListener {
            if(validateGroup()){
                var groupList = adapter.getSelectedUsers().toMutableList()
                groupList.add(CurrentUser.userId!!)

                val groupId = GroupRepository.create(groupName.text.toString(), spinner.selectedItem.toString(), groupList)
                GroupRepository.acceptInvite(CurrentUser.userId!!, groupId)
                navigateGroupsDashboard()
            }
        }
    }

    // helper function used to check if filled out group details are correctly formatted
    private fun validateGroup():Boolean{
        var msg = ""

        if(groupName.text.isEmpty()){
            msg = "Please name group!"
        }
        else if(adapter.getSelectedUsers().isEmpty()){
            msg = "Must include at least one member!"
        }

        if(msg.isEmpty()){
            return true
        }
        else{
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            return false
        }
    }

    // helper function used to navigate back to previous screen
    private fun navigateGroupsDashboard() {
        val dashboardIntent = Intent(this, GroupsActivity::class.java)
        startActivity(dashboardIntent)
        finishAffinity()
    }

}