package com.example.library.presentation.ui.main

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.library.R
import com.example.library.presentation.ui.main.BD.BookRepository
import com.example.library.presentation.ui.main.BD.ExternalBook
import com.example.library.presentation.ui.main.BD.LibraryBook
import com.example.library.presentation.ui.main.BD.Loan
import com.example.library.presentation.ui.main.BD.User
import com.example.library.presentation.ui.main.BD.db.App
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.util.Date


class BookDetailsActivity : AppCompatActivity() {

    private lateinit var bookTitleTextView: TextView
    private lateinit var bookAuthorTextView: TextView
    private lateinit var bookCategoryTextView: TextView
    private lateinit var bookDescriptionTextView: TextView
    private lateinit var bookCoverImageView: ImageView
    private lateinit var borrowButton: MaterialButton

    private lateinit var repository: BookRepository
    private var book: ExternalBook? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.details_book_activity)

        initViews()
        setupToolbar()

        // Получаем книгу из Intent
        book = intent.getParcelableExtra<ExternalBook>("book")

        if (book == null) {
            Toast.makeText(this, "Книга не найдена", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        displayBookInfo()

        // Настройка Repository
        val db = App.database
        repository = BookRepository(db.bookDao(), null)

        // Кнопка "Добавить" → "Забронировать"
        borrowButton.setOnClickListener {
            if (book != null) {
                borrowBook(book!!)
            }
        }
    }

    private fun initViews() {
        bookTitleTextView = findViewById(R.id.bookTitleTextView)
        bookAuthorTextView = findViewById(R.id.bookAuthorTextView)
        bookCategoryTextView = findViewById(R.id.bookCategoryTextView)
        bookDescriptionTextView = findViewById(R.id.bookDescriptionTextView)
        bookCoverImageView = findViewById(R.id.bookCoverImageView)
        borrowButton = findViewById(R.id.borrowButton)
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Детали книги"
    }

    private fun displayBookInfo() {
        bookTitleTextView.text = book?.title ?: "Неизвестно"
        bookAuthorTextView.text = "Автор: ${book?.author ?: "Неизвестно"}"
        bookCategoryTextView.text = "Раздел: ${getCategoryName(book?.categoryId ?: 1L)}"
        bookDescriptionTextView.text = book?.description ?: "Описание отсутствует"

        // Загрузка обложки
        Glide.with(this)
            .load(book?.imageUrl)
            .placeholder(R.drawable.ic_placeholder_book)
            .into(bookCoverImageView)
    }

    private fun borrowBook(book: ExternalBook) {
        lifecycleScope.launch {
            try {
                val user = getCurrentUser()
                if (user == null) {
                    Toast.makeText(this@BookDetailsActivity, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Проверяем, есть ли уже такая книга в библиотеке
                val libraryBook = findLibraryBookByExternalId(book.apiId)
                if (libraryBook == null) {
                    // Если книги нет — добавляем её
                    addBookToLibrary(book, user)
                } else {
                    // Если есть — бронируем
                    reserveBook(libraryBook, user)
                }

                Toast.makeText(this@BookDetailsActivity, "Книга забронирована!", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@BookDetailsActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun getCurrentUser(): User? {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val email = prefs.getString("userEmail", "") ?: ""
        return App.database.userDao().getUserByEmail(email)
    }


    private suspend fun addBookToLibrary(book: ExternalBook, user: User) {
        // Добавляем книгу в библиотеку (как новую)
        val categoryId = 1L // По умолчанию — Программирование
        val newLibraryBook = LibraryBook(
            bookId = 0,
            externalBookId = book.apiId,
            categoryId = categoryId,
            isElectronic = false, // Можно сделать выбор
            copiesTotal = 1,
            copiesAvailable = 1
        )
        val bookId = App.database.bookDao().insertLibraryBook(newLibraryBook)

        // Бронируем
        val loan = Loan(
            loanId = 0,
            userId = user.userId,
            bookId = bookId,
            issueDate = Date().time,
            dueDate = Date().time + (30 * 24 * 60 * 60 * 1000L), // 30 дней
            status = "active"
        )
        App.database.bookDao().insertLoan(loan)
    }

    private suspend fun reserveBook(libraryBook: LibraryBook, user: User) {
        // Проверяем, доступна ли книга
        if (libraryBook.copiesAvailable <= 0) {
            Toast.makeText(this@BookDetailsActivity, "Нет свободных экземпляров", Toast.LENGTH_SHORT).show()
            return
        }

        // Создаём запись о выдаче
        val loan = Loan(
            loanId = 0,
            userId = user.userId,
            bookId = libraryBook.bookId,
            issueDate = Date().time,
            dueDate = Date().time + (30 * 24 * 60 * 60 * 1000L),
            status = "active"
        )
        App.database.bookDao().insertLoan(loan)

        // Уменьшаем количество доступных экземпляров
        val updatedBook = libraryBook.copy(copiesAvailable = libraryBook.copiesAvailable - 1)
        App.database.bookDao().updateLibraryBook(updatedBook)
    }

    private suspend fun findLibraryBookByExternalId(externalId: String): LibraryBook? {
        return App.database.bookDao().getLibraryBookByExternalId(externalId)
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

}