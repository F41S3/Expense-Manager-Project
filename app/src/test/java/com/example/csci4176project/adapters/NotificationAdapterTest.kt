package com.example.csci4176project.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.csci4176project.R
import com.example.csci4176project.models.GroupModel
import com.example.csci4176project.repositories.GroupRepository
import com.example.csci4176project.util.CurrentUser
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class NotificationAdapterTest {

    private lateinit var notificationAdapter: NotificationAdapter

    @Mock
    private lateinit var mockView: View

    @Mock
    private lateinit var mockNotificationTitle: TextView

    @Mock
    private lateinit var mockNotificationDescription: TextView

    @Mock
    private lateinit var mockAcceptButton: Button

    @Mock
    private lateinit var mockDeclineButton: Button

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        notificationAdapter = NotificationAdapter()
        mockView.apply {
            `when`(findViewById<TextView>(R.id.notificationTitle)).thenReturn(mockNotificationTitle)
            `when`(findViewById<TextView>(R.id.notificationDescription)).thenReturn(mockNotificationDescription)
            `when`(findViewById<Button>(R.id.acceptButton)).thenReturn(mockAcceptButton)
            `when`(findViewById<Button>(R.id.declineBtn)).thenReturn(mockDeclineButton)
        }
    }

    @Test
    fun testOnBindViewHolder() {
        val mockGroupModel = GroupModel("Test Group", "Description")
        notificationAdapter.groupsList.add(mockGroupModel)
        notificationAdapter.groupIds.add("testGroupId")

        notificationAdapter.onBindViewHolder(NotificationAdapter.ViewHolder(mockView), 0)

        verify(mockNotificationTitle).text = "New Invite"
        verify(mockNotificationDescription).text = "You have been invited to join group Test Group"
    }

    @Test
    fun testSetGroupList() {
        val mockGroupList = listOf(GroupModel("Test Group", "Description"))
        val mockGroupIds = listOf("testGroupId")

        notificationAdapter.setGroupList(mockGroupList, mockGroupIds)

        assert(notificationAdapter.groupsList == mockGroupList)
        assert(notificationAdapter.groupIds == mockGroupIds)
    }
}
