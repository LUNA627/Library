package com.example.library.presentation.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Surface
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity

import androidx.cardview.widget.CardView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.library.presentation.ui.main.data.Book
import com.example.library.presentation.ui.main.data.BookAdapter
import com.google.android.material.textfield.TextInputEditText
import com.example.library.R
import com.example.library.presentation.ui.main.BD.db.App
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var userNameTextView: TextView
    private lateinit var profileIcon: ImageView
    private lateinit var searchEditText: TextInputEditText
    private lateinit var booksRecyclerView: RecyclerView

    private var maxLoanDays = 30;

    private val bookList = mutableListOf<Book>() // временный список
    private lateinit var adapter: BookAdapter



    private lateinit var studentMainContainer: LinearLayout
    private lateinit var librarianMainContainer: LinearLayout
    private lateinit var booksCard: CardView
    private lateinit var usersCard: CardView
    private lateinit var loansCard: CardView
    private lateinit var reportsCard: CardView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen)

// Получаем данные
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val userRole = prefs.getString("userRole", "student")
        val userEmail = prefs.getString("userEmail", "user")
        maxLoanDays = prefs.getInt("maxLoanDays", 30)

        // Инициализация всех View
        userNameTextView = findViewById(R.id.userName_text_view)
        profileIcon = findViewById(R.id.profile_icon)
        searchEditText = findViewById(R.id.search_edit_text)
        booksRecyclerView = findViewById(R.id.books_recycler_view)
        studentMainContainer = findViewById(R.id.studentMainContainer)
        librarianMainContainer = findViewById(R.id.librarianMainContainer)
        booksCard = findViewById(R.id.booksCard)
        usersCard = findViewById(R.id.usersCard)
        loansCard = findViewById(R.id.loansCard)
        reportsCard = findViewById(R.id.reportsCard)

        // Настройка RecyclerView (адаптер — один на всех)
        booksRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BookAdapter(bookList)
        booksRecyclerView.adapter = adapter


        profileIcon.setOnClickListener {
            if (userRole == "librarian") {
                startActivity(Intent(this, LibrarianProfileActivity::class.java))
            } else {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        }

        // Главная логика: выбор интерфейса
        setupUIByRole(userRole ?: "student", userEmail ?: "user")

    }

    private fun loadBooksFromDatabase() {
        lifecycleScope.launch {
            val db = App.database
            val libraryBooks = db.bookDao().getAllLibraryBooks()

            // Преобразуем LibraryBook → Book (для адаптера студента)
            bookList.clear()
            for (libBook in libraryBooks) {
                // Получаем ExternalBook
                val externalBook = db.bookDao().getExternalBookById(libBook.externalBookId)
                if (externalBook != null) {
                    bookList.add(
                        Book(
                            title = externalBook.title,
                            author = externalBook.author,
                            category = getCategoryName(libBook.categoryId),
                            isElectronic = libBook.isElectronic,
                            imageUrl = externalBook.imageUrl ?: "@drawable/ic_placeholder_book"
                        )
                    )
                }
            }
            adapter.notifyDataSetChanged()
        }
    }
    private fun getCategoryName(categoryId: Long): String {
        return when (categoryId) {
            1L -> "Программирование"
            2L -> "История"
            3L -> "Художественная литература"
            4L -> "Наука"
            else -> "Неизвестный раздел"
        }
    }

    private fun filterBooks(query: String) {
        lifecycleScope.launch {
            val db = App.database
            val allBooks = db.bookDao().getAllLibraryBooks()
            val filtered = allBooks.filter { libBook ->
                val external = db.bookDao().getExternalBookById(libBook.externalBookId)
                external?.title?.contains(query, ignoreCase = true) == true ||
                        external?.author?.contains(query, ignoreCase = true) == true
            }
            // Обновить адаптер
        }
    }

    private fun setupUIByRole(role: String, email: String) {
        // Устанавливаем имя
        val displayName = when (role) {
            "librarian" -> "Библиотекарь"
            "teacher" -> "Преподаватель"
            else -> email.split("@")[0].replaceFirstChar { it.uppercase() }
        }
        userNameTextView.text = displayName

        // Показываем нужный интерфейс
        when (role) {
            "librarian" -> {
                studentMainContainer.visibility = View.GONE
                librarianMainContainer.visibility = View.VISIBLE

                // Настраиваем обработчики для библиотекаря
                booksCard.setOnClickListener {
                    startActivity(Intent(this, BookManagementActivity::class.java))
                }
                usersCard.setOnClickListener {
                    startActivity(Intent(this, UserManagementActivity::class.java))
                }
                loansCard.setOnClickListener {
                    startActivity(Intent(this, LoanManagementActivity::class.java))
                }
                reportsCard.setOnClickListener {
                    Toast.makeText(this, "Отчёты", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                // Студент или преподаватель
                studentMainContainer.visibility = View.VISIBLE
                librarianMainContainer.visibility = View.GONE

            }
        }
    }

}

