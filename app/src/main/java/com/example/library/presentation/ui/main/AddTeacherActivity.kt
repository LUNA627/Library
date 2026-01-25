package com.example.library.presentation.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.library.R
import com.example.library.presentation.ui.main.BD.User
import com.example.library.presentation.ui.main.BD.db.App
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddTeacherActivity : AppCompatActivity() {

    private lateinit var fullNameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var addButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_teacher)

        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        addButton = findViewById(R.id.addButton)

        addButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val newUser = User(
                    userId = 0,
                    email = email,
                    password = password,
                    role = "teacher",
                    registrationDate = System.currentTimeMillis(),
                    isActive = true,
                    isBlocked = false,
                    fullName = fullName
                )
                App.database.userDao().insertUser(newUser)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddTeacherActivity, "Преподаватель добавлен!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}