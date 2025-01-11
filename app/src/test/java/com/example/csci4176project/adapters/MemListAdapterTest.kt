package com.example.csci4176project.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.csci4176project.R
import com.example.csci4176project.models.UserModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class MemListAdapterTest {

    private lateinit var memListAdapter: MemListAdapter

    @Before
    fun setUp() {
        memListAdapter = MemListAdapter()
    }

    @Test
    fun testGetItemCount() {
        val userList = listOf(
            UserModel("John Doe", "john@example.com", "https://example.com/profile.jpg"),
            UserModel("Jane Smith", "jane@example.com", "https://example.com/profile.jpg")
        )

        memListAdapter.setUsers(userList)

        assertEquals(2, memListAdapter.itemCount)
    }

    @Test
    fun testSetUsers() {
        val userList = listOf(
            UserModel("John Doe", "john@example.com", "https://example.com/profile.jpg"),
            UserModel("Jane Smith", "jane@example.com", "https://example.com/profile.jpg")
        )

        memListAdapter.setUsers(userList)

        assertEquals(userList, memListAdapter.userList)
    }

    @Test
    fun testBindMethodInMemListViewHolder() {
        val mockView = mock(View::class.java)
        val mockMemberName = mock(TextView::class.java)
        val mockUserProfile = mock(ImageView::class.java)
        val mockUserEmail = mock(TextView::class.java)

        `when`(mockView.findViewById<TextView>(R.id.memberItem)).thenReturn(mockMemberName)
        `when`(mockView.findViewById<ImageView>(R.id.imageView)).thenReturn(mockUserProfile)
        `when`(mockView.findViewById<TextView>(R.id.userInviteEmail)).thenReturn(mockUserEmail)

        MemListAdapter.MemListViewHolder(mockView)
    }
}
