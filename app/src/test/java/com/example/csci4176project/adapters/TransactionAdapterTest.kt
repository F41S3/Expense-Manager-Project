package com.example.csci4176project.adapters

import android.view.View
import android.widget.TextView
import com.example.csci4176project.R
import com.example.csci4176project.models.ExpenseModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class TransactionAdapterTest {

    private lateinit var transactionAdapter: TransactionAdapter

    @Before
    fun setUp() {
        transactionAdapter = TransactionAdapter()
    }

    @Test
    fun testGetItemCount() {
        val expenseList = listOf(
            ExpenseModel("Expense 1", "Description 1", "1"),
            ExpenseModel("Expense 2", "Description 2", "2")
        )

        transactionAdapter.setExpenses(expenseList)
        assertEquals(2, transactionAdapter.itemCount)
    }

    @Test
    fun testSetExpenses() {
        val expenseList = listOf(
            ExpenseModel("Expense 1", "Description 1", "1"),
            ExpenseModel("Expense 2", "Description 2", "2")
        )

        transactionAdapter.setExpenses(expenseList)
        assertEquals(expenseList, transactionAdapter.expenseList)
    }
}
