package com.example.csci4176project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.csci4176project.R
import com.example.csci4176project.models.UserModel
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

/**
 * Adapter used to display the list of members in a group
 */
class MemListAdapter(var userList: List<UserModel> = emptyList()):
    RecyclerView.Adapter<MemListAdapter.MemListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MemListViewHolder(inflater.inflate(R.layout.member_item, parent, false))
    }

    override fun onBindViewHolder(holder: MemListViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size

    class MemListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val member: TextView = view.findViewById(R.id.memberItem)
        private val userProfile: ImageView = itemView.findViewById(R.id.imageView)
        private val userEmail: TextView = itemView.findViewById(R.id.userInviteEmail)
        fun bind(user: UserModel) {
            member.text = user.name
            val id = user?.pfp?.substringAfterLast("/")
            val imageUrl = "https://firebasestorage.googleapis.com/v0/b/csci4176project-cbc40.appspot.com/o/$id?alt=media"
            Picasso.get().load(imageUrl)
                .error(R.drawable.default_profile_picture)
                .fit()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(userProfile)
            userEmail.text = user.email
        }

    }

    fun setUsers(userList: List<UserModel>) {
        this.userList = userList
    }
}