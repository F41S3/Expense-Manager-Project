package com.example.csci4176project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.csci4176project.R
import com.example.csci4176project.models.GroupModel
import com.example.csci4176project.repositories.GroupRepository
import com.example.csci4176project.util.CurrentUser

/**
 * Adapter used to display the list of notifications for a user
 */
class NotificationAdapter(
    var groupsList: MutableList<GroupModel> = mutableListOf(),
    var groupIds: MutableList<String> = mutableListOf()
) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.notification_friendreq_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friendRequest = groupsList[position]
        val groupId = groupIds[position]

        // connect two on clicks to the corresponding buttons for accept and decline
        holder.bind(friendRequest, groupId, acceptAction = {
            groupIds.removeAt(position)
            groupsList.removeAt(position)
            acceptOnClick(groupId)

        }, declineAction = {
            groupIds.removeAt(position)
            groupsList.removeAt(position)
            declineOnClick(groupId);
        })
    }

    override fun getItemCount(): Int = groupsList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val notificationTitle: TextView = view.findViewById(R.id.notificationTitle)
        private val notificationDescription: TextView = view.findViewById(R.id.notificationDescription)
        private val acceptButton: Button = view.findViewById(R.id.acceptButton)
        private val declineButton: Button = view.findViewById(R.id.declineBtn)

        fun bind(group: GroupModel, groupId: String, acceptAction: () -> Unit, declineAction: () -> Unit) {
            notificationTitle.text = "New Invite"
            notificationDescription.text = "You have been invited to join group ${group.name}"

            // Handle button click here
            acceptButton.setOnClickListener{
                acceptAction()
            }

            declineButton.setOnClickListener{
                declineAction()
            }
        }
    }

    fun setGroupList(groupList: List<GroupModel>, groupIds: List<String>) {
        this.groupsList = groupList.toMutableList()
        this.groupIds = groupIds.toMutableList()
    }

    // use the group repository function to accept the invite
    fun acceptOnClick(groupId: String){
        GroupRepository.acceptInvite(CurrentUser.userId!!, groupId)
        notifyDataSetChanged()
    }

    // use the group repository function to decline the invite
    fun declineOnClick(groupId: String){
        GroupRepository.declineInvite(CurrentUser.userId!!, groupId)
        notifyDataSetChanged()
    }
}
