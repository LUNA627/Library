package com.example.library.presentation.ui.main

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.library.R
import com.example.library.presentation.ui.main.BD.BookRepository
import com.example.library.presentation.ui.main.BD.GoogleBooksApi
import com.example.library.presentation.ui.main.BD.UserRepository
import com.example.library.presentation.ui.main.BD.db.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LibrarianProfileActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var booksCountTextView: TextView
    private lateinit var loansCountTextView: TextView
    private lateinit var blockedUsersTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_librarian_profile)

        // Инициализация View
        backButton = findViewById(R.id.backButton)
        booksCountTextView = findViewById(R.id.booksCountTextView)
        loansCountTextView = findViewById(R.id.loansCountTextView)
        blockedUsersTextView = findViewById(R.id.blockedUsersTextView)

        // Кнопка "Назад"
        backButton.setOnClickListener { finish() }

        // Загружаем статистику
        loadStatistics()
    }

    private fun loadStatistics() {
        lifecycleScope.launch {
            val db = App.database
            val retrofit = Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/books/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(GoogleBooksApi::class.java)
            val bookRepo = BookRepository(db.bookDao(), api)
            val userRepo = UserRepository(db.userDao())

            val totalBooks = bookRepo.getTotalBooks()
            val activeLoans = bookRepo.getActiveLoans()
            val blockedUsers = userRepo.getBlockedUsersCount()

            withContext(Dispatchers.Main) {
                booksCountTextView.text = "Книг в фонде: $totalBooks"
                loansCountTextView.text = "Активных выдач: $activeLoans"
                blockedUsersTextView.text = "Заблокированных: $blockedUsers"
            }
        }
    }
}