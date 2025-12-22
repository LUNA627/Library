package com.example.library.presentation.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.library.R
import com.example.library.presentation.ui.main.BD.BookRepository
import com.example.library.presentation.ui.main.BD.GoogleBooksApi
import com.example.library.presentation.ui.main.BD.db.App
import com.example.library.presentation.ui.main.data.LibraryBookAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BookManagementActivity : AppCompatActivity() {

    private lateinit var booksRecyclerView: RecyclerView
    private lateinit var addBookFab: FloatingActionButton
    private lateinit var adapter: LibraryBookAdapter
    private lateinit var repository: BookRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_management)

        // Инициализация View
        booksRecyclerView = findViewById(R.id.booksRecyclerView)
        addBookFab = findViewById(R.id.addBookFab)

        // Настройка RecyclerView
        booksRecyclerView.layoutManager = LinearLayoutManager(this)

        // Создаём репозиторий
        val db = App.database
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/books/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(GoogleBooksApi::class.java)
        repository = BookRepository(db.bookDao(), api)

        // Создаём адаптер
        adapter = LibraryBookAdapter(
            context = this,
            books = emptyList(),
            repository = repository,
            onBookDeleted = { loadBooks() }
        )
        booksRecyclerView.adapter = adapter

        // Кнопка "Добавить"
        addBookFab.setOnClickListener {
            val intent = Intent(this, SearchBookActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_BOOK)
        }

        // Кнопка "Назад" в Toolbar
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }

        // Загружаем книги
        loadBooks()

    }

    private fun loadBooks() {
        lifecycleScope.launch {
            val books = repository.getAllLibraryBooks()
            adapter.updateData(books)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_BOOK && resultCode == RESULT_OK) {
            loadBooks() // ← обновляем список!
        }
    }

    companion object {
        private const val REQUEST_ADD_BOOK = 1001 // ← любое уникальное число
    }
}