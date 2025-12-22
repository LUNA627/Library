package com.example.library.presentation.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.library.R
import com.google.android.material.button.MaterialButton

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var nextButton: MaterialButton
    private lateinit var roleRadioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.role_selection_screen)

        nextButton = findViewById(R.id.nextButton)
        roleRadioGroup = findViewById(R.id.roleRadioGroup)

        // Кнопка "ДАЛЕЕ" активна только если выбрана роль
        roleRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            nextButton.isEnabled = checkedId != -1
        }

        nextButton.setOnClickListener {
            val selectedId = roleRadioGroup.checkedRadioButtonId
            val role = when (selectedId) {
                R.id.studentRadioButton -> "student"
                R.id.teacherRadioButton -> "teacher"
                R.id.librarianRadioButton -> "librarian"
                else -> null
            }

            if (role != null) {
                val intent = Intent(this, AuthActivity::class.java)
                intent.putExtra("USER_ROLE", role)
                startActivity(intent)
                finish()
            }
        }
    }
}