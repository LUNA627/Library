package com.example.library.presentation.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.library.R
import com.example.library.presentation.ui.main.BD.BookRepository
import com.example.library.presentation.ui.main.BD.ExternalBook
import com.example.library.presentation.ui.main.BD.GoogleBooksApi
import com.example.library.presentation.ui.main.BD.db.LibraryDatabase
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.library.presentation.ui.main.BD.LibraryBook
import com.example.library.presentation.ui.main.BD.db.App
import kotlinx.coroutines.launch

class AddEditBookActivity : AppCompatActivity() {

    private lateinit var bookCover: ImageView
    private lateinit var bookTitle: TextView
    private lateinit var bookAuthor: TextView
    private lateinit var bookIsbn: TextView
    private lateinit var electronicCheckbox: MaterialCheckBox
    private lateinit var copiesInputLayout: TextInputLayout
    private lateinit var copiesEditText: TextInputEditText
    private lateinit var categorySpinner: Spinner
    private lateinit var saveButton: MaterialButton

    private lateinit var repository: BookRepository
    private lateinit var selectedBook: ExternalBook


    // Инициализация ID
    private var libraryBookId: Long = -1L
    private var currentIsElectronic: Boolean = false
    private var currentCopies: Int = 1
    private var currentCategoryId: Long = 1L
    var selectedCategoryIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_book)

        // Инициализация View
        bookCover = findViewById(R.id.bookCover)
        bookTitle = findViewById(R.id.bookTitle)
        bookAuthor = findViewById(R.id.bookAuthor)
        bookIsbn = findViewById(R.id.bookIsbn)
        categorySpinner = findViewById(R.id.categorySpinner)
        electronicCheckbox = findViewById(R.id.electronicCheckbox)
        copiesInputLayout = findViewById(R.id.copiesInputLayout)
        copiesEditText = findViewById(R.id.copiesEditText)
        saveButton = findViewById(R.id.saveButton)

        // Настройка Repository
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/books/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(GoogleBooksApi::class.java)
        val db = App.database // ← используем синглтон
        repository = BookRepository(db.bookDao(), api)

        // Список разделов
        val categories = arrayOf("Программирование", "История", "Художественная литература", "Наука")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Запомни выбранный раздел

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategoryIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        // Скрыть/показать поле количества
        electronicCheckbox.setOnCheckedChangeListener { _, isChecked ->
            copiesInputLayout.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        // Обработка кнопки "Назад"
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }


        // Проверяем режим: добавление или редактирование
        val libraryBook = intent.getParcelableExtra<LibraryBook>("library_book")
        if (libraryBook != null) {
            // Режим редактирования
            libraryBookId = libraryBook.bookId
            currentIsElectronic = libraryBook.isElectronic
            currentCopies = libraryBook.copiesTotal
            currentCategoryId = libraryBook.categoryId

            lifecycleScope.launch {
                val externalBook = repository.getExternalBookById(libraryBook.externalBookId)
                if (externalBook != null) {
                    selectedBook = externalBook
                    displayBookInfo()
                    runOnUiThread {
                        electronicCheckbox.isChecked = currentIsElectronic
                        copiesEditText.setText(currentCopies.toString())
                        saveButton.text = "Сохранить изменения"

                    }
                }
            }
        } else {
            // Режим добавления
            selectedBook = intent.getParcelableExtra<ExternalBook>("external_book")!!
            displayBookInfo()
            saveButton.text = "Добавить в фонд"
        }

        saveButton.setOnClickListener {
            saveBookToLibrary()
        }
    }


    private fun displayBookInfo() {
        bookTitle.text = selectedBook.title
        bookAuthor.text = selectedBook.author
        bookIsbn.text = selectedBook.isbn?.let { "ISBN: $it" } ?: "ISBN: —"

        if (!selectedBook.imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(selectedBook.imageUrl)
                .placeholder(R.drawable.ic_placeholder_book)
                .into(bookCover)
        } else {
            bookCover.setImageResource(R.drawable.ic_placeholder_book)
        }
    }

    private fun saveBookToLibrary() {
        val isElectronic = electronicCheckbox.isChecked
        val copies = if (isElectronic) {
            1
        } else {
            copiesEditText.text.toString().toIntOrNull() ?: 1
        }


        val categoryId = when (selectedCategoryIndex) {
            0 -> 1L
            1 -> 2L
            2 -> 3L
            else -> 4L
        }

        lifecycleScope.launch {
            try {

                repository.saveExternalBook(selectedBook)
                if (libraryBookId == -1L) {

                    repository.addLibraryBook(selectedBook, categoryId, isElectronic, copies)


                    val intent = Intent(this@AddEditBookActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    Toast.makeText(this@AddEditBookActivity, "Книга добавлена!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {

                    val updatedBook = LibraryBook(
                        bookId = libraryBookId,
                        externalBookId = selectedBook.apiId,
                        categoryId = categoryId,
                        isElectronic = isElectronic,
                        copiesTotal = if (isElectronic) 1 else copies,
                        copiesAvailable = if (isElectronic) 1 else copies
                    )
                    repository.updateLibraryBook(updatedBook)
                    val intent = Intent(this@AddEditBookActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    Toast.makeText(this@AddEditBookActivity, "Изменения сохранены!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddEditBookActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}