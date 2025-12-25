package com.example.library.presentation.ui.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.library.R
import com.example.library.presentation.ui.main.BD.User
import com.example.library.presentation.ui.main.BD.db.App
import com.example.library.presentation.ui.main.BD.db.LibraryDatabase
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class StudentRegistrationActivity : AppCompatActivity(){

    private lateinit var fullNameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var backButton: MaterialButton


    private lateinit var fullNameInputLayout: TextInputLayout
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInputLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.student_registration_screen)

        fullNameEditText = findViewById(R.id.fullName_edit_text)
        emailEditText = findViewById(R.id.email_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text)
        registerButton = findViewById(R.id.register_button)
        backButton = findViewById(R.id.back_button)

        fullNameInputLayout = findViewById(R.id.fullName_input_layout)
        emailInputLayout = findViewById(R.id.email_input_layout)
        passwordInputLayout = findViewById(R.id.password_input_layout)
        confirmPasswordInputLayout = findViewById(R.id.confirm_password_input_layout)


        registerButton.setOnClickListener { registerStudent() }
        backButton.setOnClickListener {
            finish()
        }




    }
    private fun registerStudent() {
        // Сброс ошибок
        clearErrors()

        val fullName = fullNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        var valid = true

        // Валидация ФИО
        if (fullName.isEmpty()) {
            fullNameInputLayout.error = "Введите ФИО"
            valid = false
        }

        // Валидация email
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = "Введите корректный email"
            valid = false
        }

        // Валидация пароля
        if (password.length < 6) {
            passwordInputLayout.error = "Пароль должен быть не менее 6 символов"
            valid = false
        }

        // Подтверждение пароля
        if (password != confirmPassword) {
            confirmPasswordInputLayout.error = "Пароли не совпадают"
            valid = false
        }

        if (!valid) return

        lifecycleScope.launch {
            try {
                val userId = System.currentTimeMillis()
                val registrationDate = System.currentTimeMillis()

                val user = User(
                    userId = userId,
                    email = email,
                    fullName = fullName,
                    password = password,
                    role = "student",
                    isBlocked = false,
                    registrationDate = registrationDate
                )

                // Сохраняем в базу
                val db = App.database
                db.userDao().insertUser(user)

                Toast.makeText(this@StudentRegistrationActivity, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@StudentRegistrationActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearErrors() {
        fullNameInputLayout.error = null
        emailInputLayout.error = null
        passwordInputLayout.error = null
        confirmPasswordInputLayout.error = null
    }
}