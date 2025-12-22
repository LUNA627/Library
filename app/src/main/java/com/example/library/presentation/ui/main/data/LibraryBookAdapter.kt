package com.example.library.presentation.ui.main.data

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.library.R
import com.example.library.presentation.ui.main.AddEditBookActivity
import com.example.library.presentation.ui.main.BD.BookRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.example.library.presentation.ui.main.BD.LibraryBook


class LibraryBookAdapter(
    private val context: Context,
    private var books: List<LibraryBook>,
    private val repository: BookRepository,
    private val onBookDeleted: () -> Unit // callback для обновления списка
) : RecyclerView.Adapter<LibraryBookAdapter.BookViewHolder>() {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.bookTitle)
        val author: TextView = itemView.findViewById(R.id.bookAuthor)
        val category: TextView = itemView.findViewById(R.id.bookCategory)
        val typeBadge: TextView = itemView.findViewById(R.id.typeBadge)
        val availability: TextView = itemView.findViewById(R.id.availabilityText)
        val bookCover: ImageView = itemView.findViewById(R.id.bookCover)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_library_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {


        val book = books[position]

        CoroutineScope(Dispatchers.Main).launch {
            val externalBook = repository.getExternalBookById(book.externalBookId)
            if (externalBook != null) {
                holder.title.text = externalBook.title
                holder.author.text = externalBook.author
                holder.category.text = "Раздел: ${getCategoryName(book.categoryId)}"

                val imageUrl = externalBook.imageUrl ?: externalBook.thumbnailUrl

                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(holder.itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder_book)
                        .error(R.drawable.ic_placeholder_book)
                        .into(holder.bookCover)
                } else {
                    holder.bookCover.setImageResource(R.drawable.ic_placeholder_book)
                }
            }
        }



        // Тип книги
        holder.typeBadge.text = if (book.isElectronic) "[Э]" else "[Б]"
        holder.typeBadge.setBackgroundResource(
            if (book.isElectronic) R.drawable.badge_green else R.drawable.badge_blue
        )


        // Доступность
        holder.availability.text = if (book.isElectronic) {
            "Электронная"
        } else {
            "Доступно: ${book.copiesAvailable}/${book.copiesTotal}"
        }

        // 👆 Одиночный клик — редактирование
        holder.itemView.setOnClickListener {
            val intent = Intent(context, AddEditBookActivity::class.java).apply {
                putExtra("library_book", book)
            }
            it.context.startActivity(intent)
        }

        // 👇 Долгое нажатие — удаление
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(it.context)
                .setTitle("Удалить книгу?")
                .setPositiveButton("Да") { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        repository.deleteLibraryBook(book.bookId)
                        (it.context as? androidx.appcompat.app.AppCompatActivity)?.runOnUiThread {
                            onBookDeleted()
                        }
                    }
                }
                .setNegativeButton("Нет", null)
                .show()
            true
        }

    }

    override fun getItemCount() = books.size

    fun updateData(newBooks: List<LibraryBook>) {
        this.books = newBooks
        notifyDataSetChanged()
    }

    // Вспомогательная функция — отображение категории
    private fun getCategoryName(categoryId: Long): String {
        return when (categoryId) {
            1L -> "Программирование"
            2L -> "История"
            3L -> "Художественная литература"
            4L -> "Наука"
            else -> "Другое"
        }
    }
}