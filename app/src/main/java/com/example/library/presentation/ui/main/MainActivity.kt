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
import com.example.library.presentation.ui.main.BD.ExternalBook
import com.example.library.presentation.ui.main.BD.GoogleBooksApi
import com.example.library.presentation.ui.main.BD.LibraryBook
import com.example.library.presentation.ui.main.BD.Loan
import com.example.library.presentation.ui.main.BD.db.App
import com.example.library.presentation.ui.main.data.AdminCardAdapter
import com.example.library.presentation.ui.main.data.AdminCardItem
import com.example.library.presentation.ui.main.data.DisplayBook
import com.example.library.presentation.ui.main.data.SearchBookAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



    class MainActivity : AppCompatActivity() {

        private lateinit var userNameTextView: TextView
        private lateinit var profileIcon: ImageView
        private lateinit var searchEditText: TextInputEditText
        private lateinit var booksRecyclerView: RecyclerView
        private lateinit var studentMainContainer: LinearLayout
        private lateinit var librarianMainContainer: LinearLayout

        private var allBooks = mutableListOf<DisplayBook>()

        // Адаптер и данные
        private lateinit var adapter: BookAdapter

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.main_screen)

            // Получаем роль из SharedPreferences
            val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val userRole = prefs.getString("userRole", "student") ?: "student"
            val userEmail = prefs.getString("userEmail", "user@example.com") ?: "user"

            // Инициализация View
            initViews()

            // Настройка RecyclerView
            setupRecyclerView()

            // Настройка профиля
            setupProfile(userRole)

            // Настройка интерфейса по роли
            setupUIByRole(userRole, userEmail)

            // Загружаем книги (только для студентов/преподавателей)
            if (userRole != "librarian") {
                loadBooksFromDatabase()
                setupSearch()
            }
        }

        private fun initViews() {
            userNameTextView = findViewById(R.id.userName_text_view)
            profileIcon = findViewById(R.id.profile_icon)
            searchEditText = findViewById(R.id.search_edit_text)
            booksRecyclerView = findViewById(R.id.books_recycler_view)
            studentMainContainer = findViewById(R.id.studentMainContainer)
            librarianMainContainer = findViewById(R.id.librarianMainContainer)
        }

        private fun setupRecyclerView() {
            adapter = BookAdapter(this, allBooks)
            booksRecyclerView.adapter = adapter
            booksRecyclerView.layoutManager = LinearLayoutManager(this)
        }

        private fun setupProfile(userRole: String) {
            profileIcon.setOnClickListener {
                if (userRole == "librarian") {
                    startActivity(Intent(this, LibrarianProfileActivity::class.java))
                } else {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
            }
        }

        private fun setupUIByRole(role: String, email: String) {
            val displayName = when (role) {
                "librarian" -> "Библиотекарь"
                "teacher" -> "Преподаватель"
                else -> email.split("@")[0].replaceFirstChar { it.uppercase() }
            }
            userNameTextView.text = displayName

            when (role) {
                "librarian" -> {
                    studentMainContainer.visibility = View.GONE
                    librarianMainContainer.visibility = View.VISIBLE

                    // Настройка карусели админа
                    setupAdminCarousel()
                }

                else -> {
                    studentMainContainer.visibility = View.VISIBLE
                    librarianMainContainer.visibility = View.GONE
                }
            }
        }

        private fun setupAdminCarousel() {
            val carousel = findViewById<RecyclerView>(R.id.adminCarousel)

            val items = listOf(
                AdminCardItem("Книги", R.drawable.ic_books) {
                    startActivity(Intent(this, BookManagementActivity::class.java))
                },
                AdminCardItem("Читатели", R.drawable.ic_users) {
                    startActivity(Intent(this, UserManagementActivity::class.java))
                }
            )

            val adapter = AdminCardAdapter(items) { item ->
                item.action()
            }

            carousel.adapter = adapter
            carousel.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        }

        private fun loadBooksFromDatabase() {
            lifecycleScope.launch {
                val db = App.database
                val libraryBooks = db.bookDao().getAllLibraryBooks()
                val books = mutableListOf<DisplayBook>()

                for (libBook in libraryBooks) {
                    val externalBook = db.bookDao().getExternalBookById(libBook.externalBookId)
                    if (externalBook != null) {
                        // Проверяем, добавлена ли книга пользователем
                        val isAdded = isBookAddedByUser(externalBook.title)
                        books.add(
                            DisplayBook(
                                title = externalBook.title,
                                author = externalBook.author,
                                category = getCategoryName(libBook.categoryId),
                                isElectronic = libBook.isElectronic,
                                isAdded = isAdded
                            )
                        )
                    }
                }

                withContext(Dispatchers.Main) {
                    allBooks.clear()
                    allBooks.addAll(books)
                    adapter.notifyDataSetChanged()
                }
            }
        }

        private suspend fun isBookAddedByUser(bookTitle: String): Boolean {
            val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val userEmail = prefs.getString("userEmail", "") ?: ""
            val user = App.database.userDao().getUserByEmail(userEmail) ?: return false

            val externalBooks = App.database.bookDao().getAllExternalBooks()
            val externalBook = externalBooks.find { it.title == bookTitle } ?: return false

            val libraryBook = App.database.bookDao().getLibraryBookByExternalId(externalBook.apiId) ?: return false

            val loans = App.database.bookDao().getAllLoansByUser(user.userId)
            return loans.any { it.bookId == libraryBook.bookId }
        }


        private fun setupSearch() {
            searchEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val query = s?.toString()?.trim() ?: ""
                    filterBooks(query)
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        private fun filterBooks(query: String) {
            lifecycleScope.launch {
                val db = App.database
                val libraryBooks = db.bookDao().getAllLibraryBooks()
                val filteredBooks = mutableListOf<Book>()

                for (libBook in libraryBooks) {
                    val external = db.bookDao().getExternalBookById(libBook.externalBookId)
                    if (external != null) {
                        val matches = external.title.contains(query, ignoreCase = true) ||
                                external.author.contains(query, ignoreCase = true)
                        if (matches || query.isEmpty()) {
                            filteredBooks.add(
                                Book(
                                    title = external.title,
                                    author = external.author,
                                    category = getCategoryName(libBook.categoryId),
                                    isElectronic = libBook.isElectronic
                                )
                            )
                        }
                    }
                }
            }
        }

        private fun getCategoryName(categoryId: Long): String = when (categoryId) {
            1L -> "Программирование"
            2L -> "История"
            3L -> "Художественная литература"
            4L -> "Наука"
            else -> "Неизвестный раздел"
        }

        fun addBookToUserProfile(book: DisplayBook, callback: (Boolean) -> Unit) {
            lifecycleScope.launch {
                try {
                    val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                    val userEmail = prefs.getString("userEmail", "") ?: ""
                    val user = App.database.userDao().getUserByEmail(userEmail)
                    if (user == null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    // 1. Найти ExternalBook по названию
                    val externalBooks = App.database.bookDao().getAllExternalBooks()
                    val externalBook = externalBooks.find { it.title == book.title }
                    if (externalBook == null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Книга не найдена в базе", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    // 2. Найти LibraryBook по externalBookId
                    val libraryBook = App.database.bookDao().getLibraryBookByExternalId(externalBook.apiId)
                    if (libraryBook == null) {
                        // Книги нет в библиотеке — добавляем
                        val newLibBook = LibraryBook(
                            bookId = 0,
                            externalBookId = externalBook.apiId,
                            categoryId = 1L,
                            isElectronic = book.isElectronic,
                            copiesTotal = 1,
                            copiesAvailable = 1
                        )
                        val libBookId = App.database.bookDao().insertLibraryBook(newLibBook)

                        // 3. Создаём Loan с ПРАВИЛЬНЫМ bookId
                        val loan = Loan(
                            loanId = 0,
                            userId = user.userId,
                            bookId = libBookId, // ← ПРАВИЛЬНО!
                            issueDate = System.currentTimeMillis(),
                            dueDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
                            status = "active"
                        )
                        App.database.bookDao().insertLoan(loan)
                    } else {
                        // Книга уже есть — используем её bookId
                        val loan = Loan(
                            loanId = 0,
                            userId = user.userId,
                            bookId = libraryBook.bookId, // ← ПРАВИЛЬНО!
                            issueDate = System.currentTimeMillis(),
                            dueDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
                            status = "active"
                        )
                        App.database.bookDao().insertLoan(loan)
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Книга добавлена!", Toast.LENGTH_SHORT).show()
                        callback(true)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
