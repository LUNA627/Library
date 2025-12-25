package com.example.library.presentation.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.library.R
import com.example.library.presentation.ui.main.BD.User
import com.example.library.presentation.ui.main.BD.db.App
import com.example.library.presentation.ui.main.BD.db.LibraryDatabase
import com.example.library.presentation.ui.main.data.UserAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserManagementActivity : AppCompatActivity() {

    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        adapter = UserAdapter(emptyList()) { user ->
            toggleBlockUser(user)
        }
        usersRecyclerView.adapter = adapter
        usersRecyclerView.layoutManager = LinearLayoutManager(this)

        loadUsers()

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        findViewById<FloatingActionButton>(R.id.addTeacherFab).setOnClickListener {
            startActivity(Intent(this, AddTeacherActivity::class.java))
        }
    }

    private fun toggleBlockUser(user: User) {
        lifecycleScope.launch {
            val updatedUser = user.copy(isBlocked = !user.isBlocked)
            App.database.userDao().updateUser(updatedUser)
            loadUsers() // Обновить список
        }
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            val users = App.database.userDao().getAllReaders()
            withContext(Dispatchers.Main) {
                adapter.updateData(users)
            }
        }
    }
}