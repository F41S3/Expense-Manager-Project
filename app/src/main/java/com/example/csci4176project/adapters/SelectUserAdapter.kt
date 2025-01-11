package com.example.csci4176project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.csci4176project.models.UserModel
import com.example.csci4176project.R
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

class SelectUserAdapter(var userList: List<UserModel> = emptyList(), var userIds:  List<String> = emptyList(), var preselectedIds: Set<String> = emptySet()) :
    RecyclerView.Adapter<SelectUserAdapter.ViewHolder>() {
    private var selectedUsers = mutableSetOf<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_member_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
        holder.checkBox.isChecked = selectedUsers.contains(userIds[position])
        holder.checkBox.setOnClickListener {
            val isChecked = holder.checkBox.isChecked
            if (isChecked) {
                selectedUsers.add(userIds[position])
            } else {
                selectedUsers.remove(userIds[position])
            }
        }
    }

    override fun getItemCount(): Int = userList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userProfile: ImageView = itemView.findViewById(R.id.imageView)
        val userName: TextView = itemView.findViewById(R.id.userInviteName)
        val userEmail: TextView = itemView.findViewById(R.id.userInviteEmail)
        val checkBox: CheckBox = itemView.findViewById(R.id.userInviteBox)
        fun bind(user: UserModel) {
            val id = user?.pfp?.substringAfterLast("/")
            val imageUrl = "https://firebasestorage.googleapis.com/v0/b/csci4176project-cbc40.appspot.com/o/$id?alt=media"
            Picasso.get().load(imageUrl)
                .error(R.drawable.default_profile_picture)
                .fit()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(userProfile)
            userName.text = user.name
            userEmail.text = user.email
        }
    }

    fun getSelectedUsers(): Set<String> {
        return selectedUsers
    }

    fun setSelectedUsers(users: MutableSet<String>){
        selectedUsers = users
        notifyDataSetChanged()
    }

    fun setUserList(userList: List<UserModel>, userIds: List<String>) {
        this.userList = userList
        this.userIds = userIds
        notifyDataSetChanged()
    }
}
