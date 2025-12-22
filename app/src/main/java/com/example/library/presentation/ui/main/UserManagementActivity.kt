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
import com.example.library.presentation.ui.main.BD.db.LibraryDatabase
import com.example.library.presentation.ui.main.data.UserAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class UserManagementActivity : AppCompatActivity() {

    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        adapter = UserAdapter(emptyList())
        usersRecyclerView.adapter = adapter
        usersRecyclerView.layoutManager = LinearLayoutManager(this)

        loadUsers()

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }

        val addTeacherFab = findViewById<FloatingActionButton>(R.id.addTeacherFab)
        addTeacherFab.setOnClickListener {
            val intent = Intent(this, AddTeacherActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            val db = Room.databaseBuilder(this@UserManagementActivity, LibraryDatabase::class.java, "library.db").build()
            val users = db.userDao().getAllReaders()
            adapter.updateData(users)
            Log.d("UserManagement", "Найдено читателей: ${users.size}")
        }
    }
}