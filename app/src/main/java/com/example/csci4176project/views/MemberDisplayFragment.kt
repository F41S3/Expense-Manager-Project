package com.example.csci4176project.views

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.csci4176project.R
import com.example.csci4176project.adapters.SelectUserAdapter
import com.example.csci4176project.models.UserModel
import com.example.csci4176project.repositories.UserRepository
import com.example.csci4176project.util.CurrentUser

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private var adapter: SelectUserAdapter = SelectUserAdapter()
private lateinit var userInviteList: RecyclerView
private lateinit var progressBar: ProgressBar
val userList = mutableListOf<UserModel>()

/**
 * A simple [Fragment] subclass.
 * Use the [MemberDisplayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MemberDisplayFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    fun getAdapter(): SelectUserAdapter {
        return adapter
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_member_display, container, false)

        progressBar = view.findViewById(R.id.progressBar)
        userInviteList = view.findViewById(R.id.searchList)
        userInviteList.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
        userInviteList.adapter = adapter

        return view
    }
     fun handleAdapter(){
         userList.clear()
         adapter.setSelectedUsers(mutableSetOf())
         UserRepository.getAllButCurrentUser(CurrentUser.userId!!) { userIds ->
             for (id in userIds) {
                UserRepository.read(id) { user ->
                    if (user != null) {
                        userList.add(user)
                        adapter.setUserList(userList, userIds)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    fun loadProgressBar(){
        progressBar.visibility = View.VISIBLE
    }

    fun closeProgressBar(){
        progressBar.visibility = View.GONE
    }

    fun listClear(){
        userList.clear()
        adapter.notifyDataSetChanged()
    }
    fun searchProfiles(text: String){
        UserRepository.searchUsersButCurrent(CurrentUser.userId!!, text){ userIds ->
            for (id in userIds) {
                UserRepository.read(id) { user ->
                    if (user != null) {
                        userList.add(user)
                        adapter.setUserList(userList, userIds)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }
}