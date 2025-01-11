package com.example.csci4176project.adapters

import android.view.View
import android.widget.TextView
import com.example.csci4176project.models.GroupModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import com.example.csci4176project.R

class GroupsAdapterTest {

    private lateinit var groupsAdapter: GroupsAdapter

    @Mock
    private lateinit var contextMock: MockedConstruction.Context

    private lateinit var groupList: List<GroupModel>
    private lateinit var groupIds: List<String>

    @Before
    fun setUp() {
        groupList = listOf(
            GroupModel("Group 1", "Description 1"),
            GroupModel("Group 2", "Description 2")
        )
        groupIds = listOf("id1", "id2")

        groupsAdapter = GroupsAdapter(groupList, groupIds)
    }


    @Test
    fun getItemCount_ReturnsCorrectSize() {
        val mockGroupsList = listOf(
            GroupModel("Group 1", "Description 1"),
            GroupModel("Group 2", "Description 2")
        )
        val mockGroupIds = listOf("1", "2")
        groupsAdapter.setGroupList(mockGroupsList, mockGroupIds)

        assertEquals(2, groupsAdapter.getItemCount())
    }

    @Test
    fun getItemCount_ReturnsZero() {
        val mockGroupsList = emptyList<GroupModel>()
        val mockGroupIds = emptyList<String>()
        groupsAdapter.setGroupList(mockGroupsList, mockGroupIds)

        assertEquals(0, groupsAdapter.getItemCount())
    }

    @Test
    fun getItemCount_ReturnsOne() {
        val mockGroupsList = listOf(
            GroupModel("Group 1", "Description 1")
        )
        val mockGroupIds = listOf("1")
        groupsAdapter.setGroupList(mockGroupsList, mockGroupIds)

        assertEquals(1, groupsAdapter.getItemCount())
    }

    @Test
    fun onBindViewHolder_bindsDataCorrectly() {
//      test binding
        val mockView = mock(View::class.java)
        val mockGroupNameText = mock(TextView::class.java)
        val mockGroupDesc = mock(TextView::class.java)
        `when`(mockView.findViewById<TextView>(R.id.groupName)).thenReturn(mockGroupNameText)
        `when`(mockView.findViewById<TextView>(R.id.descriptionTextView)).thenReturn(mockGroupDesc)
        val viewHolder = GroupsAdapter.ViewHolder(mockView)

//       Execute the method under test.
        groupsAdapter.onBindViewHolder(viewHolder, 0)

//      verify that the data was bound correctly
        verify(mockGroupNameText).text = "Group 1"
        verify(mockGroupDesc).text = "Description 1"
    }

}