package com.example.library.presentation.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.library.R
import com.example.library.presentation.ui.main.BD.BookRepository
import com.example.library.presentation.ui.main.BD.ExternalBook
import com.example.library.presentation.ui.main.BD.GoogleBooksApi
import com.example.library.presentation.ui.main.BD.IndustryIdentifier
import com.example.library.presentation.ui.main.BD.db.App
import com.example.library.presentation.ui.main.BD.db.LibraryDatabase
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchBookActivity : AppCompatActivity() {

    private lateinit var searchEditText: TextInputEditText
    private lateinit var searchButton: MaterialButton
    private lateinit var resultsRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var adapter: SearchResultAdapter
    private lateinit var repository: BookRepository



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_book)
        Log.d("Test", "Hello from SearchActivity")
        Log.d("Search", "Activity started")

        // Инициализация View
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView)
        progressBar = findViewById(R.id.progressBar)

        // Настройка RecyclerView
        adapter = SearchResultAdapter(emptyList()) { selectedBook ->
            val intent = Intent(this, AddEditBookActivity::class.java).apply {
                putExtra("external_book", selectedBook)
            }
            startActivity(intent)
        }
        resultsRecyclerView.adapter = adapter
        resultsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Настройка Repository
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/books/v1/") // ← УБРАЛ ПРОБЕЛЫ!
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(GoogleBooksApi::class.java)
        val db = App.database
        repository = BookRepository(db.bookDao(), api)

        // Обработчик поиска
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            Log.d("Search", "Кнопка нажата, запрос: '$query'")
            if (query.isNotEmpty()) {
                searchBooks(query) // ← вызываем правильный метод
            }
        }

        // Кнопка "Назад"
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun searchBooks(query: String) {
        progressBar.visibility = View.VISIBLE
        resultsRecyclerView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                Log.d("Search", "Запрос: $query")

                val books = repository.searchBooks(query)

                Log.d("Search", "Найдено книг: ${books.size}")
                if (books.isNotEmpty()) {
                    Log.d("Search", "Первая книга: ${books[0].title}")
                }

                adapter.updateData(books)
                resultsRecyclerView.visibility = View.VISIBLE
            } catch (e: Exception) {
                Log.e("Search", "Ошибка поиска", e)
                Toast.makeText(this@SearchBookActivity, "Ошибка поиска: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}