package com.example.library.presentation.ui.main

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.library.R
import com.example.library.presentation.ui.main.BD.BookRepository
import com.example.library.presentation.ui.main.BD.GoogleBooksApi
import com.example.library.presentation.ui.main.BD.UserRepository
import com.example.library.presentation.ui.main.BD.db.App
import com.example.library.presentation.ui.main.data.ActiveBook
import com.example.library.presentation.ui.main.data.ActiveBookAdapter
import com.example.library.presentation.ui.main.data.DebtBook
import com.example.library.presentation.ui.main.data.DebtBooksAdapter
import com.example.library.presentation.ui.main.data.TakenBook
import com.example.library.presentation.ui.main.data.TakenBooksAdapter
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    // === ВСЕ ТВОИ СТАРЫЕ ПОЛЯ ===
    private lateinit var backButton: ImageView
    private lateinit var tabPersonal: TextView
    private lateinit var tabBooks: TextView
    private lateinit var tabDebts: TextView
    private lateinit var activeBooksRecyclerView: RecyclerView
    private lateinit var adapter: ActiveBookAdapter
    private lateinit var personalInfoContainer: LinearLayout
    private lateinit var registrationDateTextView: TextView
    private lateinit var totalBooksTextView: TextView
    private lateinit var activeBooksCountTextView: TextView
    private lateinit var readBooksTextView: TextView
    private lateinit var emailTextView: TextView

    private lateinit var debtsRecyclerView: RecyclerView
    private lateinit var debtBooksAdapter: DebtBooksAdapter
    private lateinit var debtsContainer: LinearLayout


    private var userRole = "student"
    private var userEmail = "user@example.com"
    private var maxLoanDays = 30

    // Модель для активных книг
    private val activeBooks = mutableListOf<ActiveBook>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_screen)

        // Инициализация View
        initViews()
        initRecyclerViews()
        loadUserData()
        setupTabs()
        setupBackButton()

        showStudentProfile()
    }

    private fun initViews() {
        backButton = findViewById(R.id.back_button)
        tabPersonal = findViewById(R.id.tab_personal)
        tabBooks = findViewById(R.id.tab_books)
        tabDebts = findViewById(R.id.tab_debts)
        activeBooksRecyclerView = findViewById(R.id.active_books_recycler_view)
        personalInfoContainer = findViewById(R.id.personalInfoContainer)
        registrationDateTextView = findViewById(R.id.registrationDateTextView)
        totalBooksTextView = findViewById(R.id.totalBooksTextView)
        activeBooksCountTextView = findViewById(R.id.activeBooksCountTextView)
        readBooksTextView = findViewById(R.id.readBooksTextView)
        emailTextView = findViewById(R.id.emailTextView)
        debtsContainer = findViewById(R.id.debts_container)


    }

    private fun initRecyclerViews() {
        activeBooksRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ActiveBookAdapter(activeBooks)
        activeBooksRecyclerView.adapter = adapter

        debtsRecyclerView = findViewById(R.id.debtsRecyclerView)
        debtsRecyclerView.layoutManager = LinearLayoutManager(this)
        debtBooksAdapter = DebtBooksAdapter(emptyList())
        debtsRecyclerView.adapter = debtBooksAdapter
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        userRole = prefs.getString("userRole", "student") ?: "student"
        userEmail = prefs.getString("userEmail", "user@example.com") ?: "user@example.com"
        maxLoanDays = prefs.getInt("maxLoanDays", 30)
    }

    private fun setupTabs() {
        tabPersonal.setOnClickListener { selectTab("personal") }
        tabBooks.setOnClickListener { selectTab("books") }
        tabDebts.setOnClickListener { selectTab("debts") }
    }

    private fun setupBackButton() {
        backButton.setOnClickListener { finish() }
    }



    private fun showStudentProfile() {
            tabBooks.visibility = View.VISIBLE
            tabDebts.visibility = View.VISIBLE
            personalInfoContainer.visibility = View.VISIBLE

            loadPersonalInfoFromDb()
            loadActiveBooksFromDb()

    }


    private fun loadPersonalInfoFromDb() {
        lifecycleScope.launch {
            val db = App.database
            val user = db.userDao().getUserByEmail(userEmail)

            withContext(Dispatchers.Main) {
                if (user != null) {
                    emailTextView.text = "Email: ${user.email}"
                    val regDate = Date(user.registrationDate).toSimpleString()
                    registrationDateTextView.text = "Зарегистрирован: $regDate"

                    // Загружаем статистику
                    loadUserStatistics(user.userId)
                } else {
                    emailTextView.text = "Email: $userEmail"
                    registrationDateTextView.text = "Зарегистрирован: —"
                    totalBooksTextView.text = "Всего книг: —"
                    activeBooksCountTextView.text = "Активных: —"
                    readBooksTextView.text = "Прочитано: —"
                }
            }
        }
    }

    private fun loadUserStatistics(userId: Long) {
        lifecycleScope.launch {
            val db = App.database
            val allLoans = db.bookDao().getAllLoansByUser(userId)
            val activeLoans = db.bookDao().getActiveLoansByUser(userId)
            val returnedLoans = db.bookDao().getReturnedLoansByUser(userId)

            withContext(Dispatchers.Main) {
                totalBooksTextView.text = "Всего книг: ${allLoans.size}"
                activeBooksCountTextView.text = "Активных: ${activeLoans.size}"
                readBooksTextView.text = "Прочитано: ${returnedLoans.size}"
            }
        }
    }

    private fun loadActiveBooksFromDb() {
        lifecycleScope.launch {
            val db = App.database
            val user = db.userDao().getUserByEmail(userEmail)
            if (user == null) return@launch

            val activeLoans = db.bookDao().getActiveLoansByUser(user.userId)
            val activeBookList = mutableListOf<ActiveBook>()

            for (loan in activeLoans) {
                // Получаем книгу
                val libraryBook = db.bookDao().getLibraryBookById(loan.bookId)
                if (libraryBook == null) continue

                val externalBook = db.bookDao().getExternalBookById(libraryBook.externalBookId)
                if (externalBook == null) continue

                val issueDate = Date(loan.issueDate).toSimpleString()
                val returnDate = Date(loan.dueDate).toSimpleString()

                activeBookList.add(
                    ActiveBook(
                        title = externalBook.title,
                        issueDate = issueDate,
                        returnDate = returnDate,
                        isElectronic = libraryBook.isElectronic,
                        status = "🟢"
                    )
                )
            }

            withContext(Dispatchers.Main) {
                activeBooks.clear()
                activeBooks.addAll(activeBookList)
                adapter.notifyDataSetChanged()
            }
        }
    }
    fun Date.toSimpleString(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(this)
    }

    private fun selectTab(tab: String) {
        resetTabs()
        when (tab) {
            "personal" -> {
                tabPersonal.setTextColor(ContextCompat.getColor(this, R.color.purple_500))
                tabPersonal.background = ContextCompat.getDrawable(this, R.drawable.tab_selected)
                personalInfoContainer.visibility = View.VISIBLE
                activeBooksRecyclerView.visibility = View.GONE
                debtsContainer.visibility = View.GONE
            }
            "books" -> {
                tabBooks.setTextColor(ContextCompat.getColor(this, R.color.purple_500))
                tabBooks.background = ContextCompat.getDrawable(this, R.drawable.tab_selected)
                personalInfoContainer.visibility = View.GONE
                activeBooksRecyclerView.visibility = View.VISIBLE
                debtsContainer.visibility = View.GONE
                loadTakenBooks()
            }
            "debts" -> {
                tabDebts.setTextColor(ContextCompat.getColor(this, R.color.purple_500))
                tabDebts.background = ContextCompat.getDrawable(this, R.drawable.tab_selected)
                personalInfoContainer.visibility = View.GONE
                activeBooksRecyclerView.visibility = View.GONE
                debtsContainer.visibility = View.VISIBLE
                loadDebts()
            }
        }
    }

    private fun resetTabs() {
        listOf(tabPersonal, tabBooks, tabDebts).forEach { tab ->
            tab.setTextColor(ContextCompat.getColor(this, R.color.gray_light))
            tab.background = ContextCompat.getDrawable(this, R.drawable.tab_unselected)
        }
    }

    private fun loadTakenBooks() {
        // Твой код для вкладки "КНИГИ"
    }

    private fun loadDebts() {
        // Твой код для вкладки "ДОЛГИ"
    }
}