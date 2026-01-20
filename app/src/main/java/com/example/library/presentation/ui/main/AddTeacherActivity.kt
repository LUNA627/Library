package com.example.library.presentation.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.library.R
import com.example.library.presentation.ui.main.BD.User
import com.example.library.presentation.ui.main.BD.db.App
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Date

class AddTeacherActivity : AppCompatActivity() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var fullNameEditText: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_teacher)

        initViews()
        setupBackButton()
        setupSaveButton()
    }

    private fun initViews() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        fullNameEditText = findViewById(R.id.fullNameEditText)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)
    }

    private fun setupBackButton() {
        backButton.setOnClickListener { finish() }
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val fullName = fullNameEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.contains("@")) {
                Toast.makeText(this, "Некорректный email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val userDao = App.database.userDao()
                    val newUser = User(
                        email = email,
                        password = password,
                        fullName = fullName,
                        role = "teacher",
                        registrationDate = Date().time,
                        isBlocked = false
                    )
                    App.database.userDao().insertUser(newUser)
                    val intent = Intent(this@AddTeacherActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    Toast.makeText(this@AddTeacherActivity, "Преподаватель добавлен!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@AddTeacherActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}