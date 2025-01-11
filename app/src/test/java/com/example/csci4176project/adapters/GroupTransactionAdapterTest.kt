package com.example.csci4176project.adapters

import com.example.csci4176project.models.ExpenseModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class GroupTransactionAdapterTest {

    @Mock
    private lateinit var viewHolderMock: GroupTransactionAdapter.ViewHolder

    private lateinit var adapter: GroupTransactionAdapter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        adapter = GroupTransactionAdapter()
    }

    @Test
    fun getItemCount_returnsCorrectCount() {
        val expenses = listOf(
            ExpenseModel("Expense 1", "Description 1", "Type 1", 100.0, "payerId", true, emptyMap(), System.currentTimeMillis()),
            ExpenseModel("Expense 2", "Description 2", "Type 2", 200.0, "payerId", true, emptyMap(), System.currentTimeMillis())
        )
        adapter.setExpenses(expenses, listOf("id1", "id2"))

        val count = adapter.itemCount

        assert(count == 2)
    }

    @Test
    fun getItemCount_returnsZero() {
        val count = adapter.itemCount
        assert(count == 0)
    }

    @Test
    fun getItemCount_ReturnsOne() {
        val mockGroupsList = listOf(
            ExpenseModel("Expense 1", "Description 1", "Type 1", 100.0, "payerId", true, emptyMap(), System.currentTimeMillis())
        )
        val mockGroupIds = listOf("1")
        adapter.setExpenses(mockGroupsList, mockGroupIds)

        Assert.assertEquals(1, adapter.getItemCount())
    }

}
