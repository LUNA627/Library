package com.example.library.presentation.ui.main

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
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
import com.example.library.presentation.ui.main.data.Book
import com.example.library.presentation.ui.main.data.DebtBook
import com.example.library.presentation.ui.main.data.DebtBooksAdapter
import com.example.library.presentation.ui.main.data.TakenBook
import com.example.library.presentation.ui.main.data.TakenBooksAdapter
import com.example.library.presentation.ui.main.data.UserBookItem
import com.example.library.presentation.ui.main.data.UserBooksAdapter
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var tabBooks: TextView
    private lateinit var tabDebts: TextView
    private lateinit var userNameTextView: TextView
    private lateinit var booksRecyclerView: RecyclerView
    private lateinit var debtsPlaceholder: TextView

    private lateinit var booksAdapter: UserBooksAdapter

    private var userEmail = "user@example.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_screen)
        initViews()
        loadUserData()
        setupTabs()
        setupBackButton()

        // По умолчанию — вкладка КНИГИ
        showBooksTab()
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        userEmail = prefs.getString("userEmail", "user@example.com") ?: "user@example.com"
        val displayName = userEmail.split("@")[0].replaceFirstChar { it.uppercase() }
        userNameTextView.text = displayName
    }

    private fun initViews() {
        backButton = findViewById(R.id.back_button)
        tabBooks = findViewById(R.id.tab_books)
        tabDebts = findViewById(R.id.tab_debts)
        userNameTextView = findViewById(R.id.userName_text_view)
        booksRecyclerView = findViewById(R.id.booksRecyclerView)
        debtsPlaceholder = findViewById(R.id.debtsPlaceholder)

        booksRecyclerView.layoutManager = LinearLayoutManager(this)


        booksAdapter = UserBooksAdapter(
            context = this,
            books = emptyList(),
            onExtendClick = { bookItem ->
                extendLoan(bookItem)
            },
            onReturnClick = { bookItem ->
                returnBook(bookItem)
            }
        )
        booksRecyclerView.adapter = booksAdapter
    }


    private fun returnBook(bookItem: UserBookItem) {
        lifecycleScope.launch {
            try {
                // 1. Получаем выдачу
                val loan = App.database.bookDao().getLoanById(bookItem.loanId)
                if (loan == null || loan.status == "returned") {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "Книга уже возвращена", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // 2. Обновляем статус на "returned"
                val updatedLoan = loan.copy(status = "returned")
                App.database.bookDao().updateLoan(updatedLoan)

                // 3. Увеличиваем количество доступных копий
                val libBook = App.database.bookDao().getLibraryBookById(loan.bookId)
                if (libBook != null && !libBook.isElectronic) {
                    val updatedLibBook = libBook.copy(
                        copiesAvailable = libBook.copiesAvailable + 1
                    )
                    App.database.bookDao().updateLibraryBook(updatedLibBook)
                }

                // 4. Обновляем список
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Книга успешно возвращена!", Toast.LENGTH_SHORT).show()
                    loadUserBooks()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Ошибка при возврате: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }






    private fun extendLoan(bookItem: UserBookItem) {
        lifecycleScope.launch {
            try {
                val loan = App.database.bookDao().getLoanById(bookItem.loanId)
                if (loan == null || loan.isExtended) return@launch

                // Обновляем дату возврата (+15 дней)
                val newDueDate = loan.dueDate + (15L * 24 * 60 * 60 * 1000)
                val updatedLoan = loan.copy(
                    dueDate = newDueDate,
                    isExtended = true
                )

                App.database.bookDao().updateLoan(updatedLoan)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Срок продлён на 15 дней!", Toast.LENGTH_SHORT).show()
                    loadUserBooks() // Обновить список
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun setupTabs() {
        tabBooks.setOnClickListener { showBooksTab() }
        tabDebts.setOnClickListener { showDebtsTab() }
    }

    private fun setupBackButton() {
        backButton.setOnClickListener { finish() }
    }

    private fun showBooksTab() {
        tabBooks.setTextColor(ContextCompat.getColor(this, R.color.white))
        tabBooks.background = ContextCompat.getDrawable(this, R.drawable.tab_selected)
        tabDebts.setTextColor(ContextCompat.getColor(this, R.color.gray_light))
        tabDebts.background = ContextCompat.getDrawable(this, R.drawable.tab_unselected)

        booksRecyclerView.visibility = View.VISIBLE
        debtsPlaceholder.visibility = View.GONE

        loadUserBooks()
    }

    private fun showDebtsTab() {
        tabDebts.setTextColor(ContextCompat.getColor(this, R.color.white))
        tabDebts.background = ContextCompat.getDrawable(this, R.drawable.tab_selected)
        tabBooks.setTextColor(ContextCompat.getColor(this, R.color.gray_light))
        tabBooks.background = ContextCompat.getDrawable(this, R.drawable.tab_unselected)

        booksRecyclerView.visibility = View.GONE
        debtsPlaceholder.visibility = View.VISIBLE
    }

    private fun loadUserBooks() {
        lifecycleScope.launch {
            val db = App.database
            val user = App.database.userDao().getUserByEmail(userEmail)
            if (user == null) return@launch

            val loans = db.bookDao().getAllLoansByUser(user.userId)
                .filter { it.status != "returned" }

            val bookList = mutableListOf<UserBookItem>()

            for (loan in loans) {
                val libBook = db.bookDao().getLibraryBookById(loan.bookId)
                val extBook = db.bookDao().getExternalBookById(libBook?.externalBookId ?: "")
                if (libBook == null || extBook == null) continue

                val returnDate = if (!libBook.isElectronic && loan.dueDate > 0) {
                    Date(loan.dueDate).toSimpleString()
                } else {
                    "Электронная"
                }

                bookList.add(
                    UserBookItem(
                        title = extBook.title,
                        returnInfo = returnDate,
                        loanId = loan.loanId,
                        canExtend = !loan.isExtended && !libBook.isElectronic
                    )
                )
            }

            withContext(Dispatchers.Main) {
                booksAdapter.updateData(bookList)
            }
        }
    }

    fun Date.toSimpleString(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(this)
    }

    override fun onResume() {
        super.onResume()
        loadUserBooks()
    }
}