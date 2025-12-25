package com.example.library.presentation.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.library.R
import com.example.library.presentation.ui.main.BD.db.App
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

class AuthActivity : AppCompatActivity() {
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var loginButton: MaterialButton
    private lateinit var emailInputLayout: TextInputLayout

    private lateinit var passwordInputLayout: TextInputLayout


    private var userRole: String = "student"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth_screen)

        emailEditText = findViewById(R.id.email_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        registerButton = findViewById(R.id.register_button)
        loginButton = findViewById(R.id.login_button)
        emailInputLayout = findViewById(R.id.email_input_layout)

        passwordInputLayout = findViewById(R.id.password_input_layout)

        userRole = intent.getStringExtra("USER_ROLE") ?: "student"

        setupUIByRole()

        registerButton.setOnClickListener {

            val intent = Intent(this, StudentRegistrationActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            attemptLogin()
        }


    }

    companion object {
        private const val LIBRARIAN_EMAIL = "l@l.l"
        private const val LIBRARIAN_PASSWORD = "admin123"
    }

    private fun setupUIByRole() {
        when (userRole) {
            "student" -> {
                findViewById<TextView>(R.id.titleTextView).text = "Вход для студента"
                registerButton.visibility = View.VISIBLE
            }
            "teacher" -> {
                findViewById<TextView>(R.id.titleTextView).text = "Вход для преподавателя"
                registerButton.visibility = View.GONE // ← НЕТ регистрации
            }
            "librarian" ->  {
                findViewById<TextView>(R.id.titleTextView).text = "Вход для библиотекаря"
                registerButton.visibility = View.GONE
            }
        }
    }


    private fun attemptLogin() {
        emailInputLayout.error = null
        passwordInputLayout.error = null

        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        var valid = true

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = "Введите корректный email"
            valid = false
        }

        if (password.isEmpty()) {
            passwordInputLayout.error = "Введите пароль"
            valid = false
        }

        if (!valid) return

        lifecycleScope.launch {
            var loginSuccess = false
            var errorMessage = "Неверный email или пароль"

            when (userRole) {
                "librarian" -> {
                    loginSuccess = email == LIBRARIAN_EMAIL && password == LIBRARIAN_PASSWORD
                    errorMessage = "Неверный логин или пароль библиотекаря"
                }
                "teacher", "student" -> {
                    val db = App.database
                    val user = db.userDao().getUserByEmail(email)
                    if (user == null) {
                        errorMessage = "Пользователь не найден"
                    } else if (user.password != password) {
                        errorMessage = "Неверный пароль"
                    } else if (user.role != userRole) {

                        errorMessage = when (userRole) {
                            "teacher" -> "Пользователь не является преподавателем"
                            "student" -> "Пользователь не является студентом"
                            else -> "Неверная роль"
                        }
                    } else if (user.isBlocked) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AuthActivity, "Ваш аккаунт заблокирован", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    } else {
                        loginSuccess = true
                    }
                }
            }

            if (loginSuccess) {
                saveLoginState(email, userRole)
                withContext(Dispatchers.Main) {
                    startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                    finish()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AuthActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun saveLoginState(email: String, role: String) {

        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val maxLoanDays = when(role) {
            "teacher" -> 60
            else -> 30
        }

        with(prefs.edit()) {
            putBoolean("isLoggedIn", true)
            putString("userEmail", email)
            putString("userRole", role)
            putInt("maxLoanDays", maxLoanDays)
            apply()
        }
    }



}