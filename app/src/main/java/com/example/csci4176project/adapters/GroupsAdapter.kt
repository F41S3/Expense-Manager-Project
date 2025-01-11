package com.example.csci4176project.adapters

import android.content.Intent
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.csci4176project.models.GroupModel
import com.example.csci4176project.R
import com.example.csci4176project.views.GroupInfoActivity

/**
 * Adapter used to display the list of groups in the overview
 */
class GroupsAdapter(private var groupsList: List<GroupModel> = emptyList(),  var groupIds:  List<String> = emptyList()) :
    RecyclerView.Adapter<GroupsAdapter.ViewHolder>() {

        private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.group_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groupsList[position]

        // if a group item is clicked, you should show more information regarding that group
        holder.itemView.setOnClickListener{
            val groupId = groupIds[position]
            val intent = Intent(context, GroupInfoActivity::class.java).apply {
                putExtra("groupId", groupId)
            }
            context.startActivity(intent)
        }
        holder.bind(group)
    }

    override fun getItemCount(): Int = groupsList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupNameText = itemView.findViewById<TextView>(R.id.groupName)
        val groupDesc = itemView.findViewById<TextView>(R.id.descriptionTextView)
        fun bind(group: GroupModel) {
            groupNameText.text = group.name
            groupDesc.text = group.type
        }
    }

    fun setGroupList(groupList: List<GroupModel>, groupIds: List<String>) {
        this.groupsList = groupList
        this.groupIds = groupIds
    }
}

