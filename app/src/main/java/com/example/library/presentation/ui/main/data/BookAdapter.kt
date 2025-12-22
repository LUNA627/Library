package com.example.library.presentation.ui.main.data

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.library.R
import com.example.library.presentation.ui.main.BookDetailsActivity
import com.google.android.material.button.MaterialButton

class BookAdapter(private var books: List<Book>) :
    RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coverImageView: ImageView = itemView.findViewById(R.id.bookCoverImageView)
        val titleTextView: TextView = itemView.findViewById(R.id.bookTitleTextView)
        val authorTextView: TextView = itemView.findViewById(R.id.bookAuthorTextView)
        val categoryTextView: TextView = itemView.findViewById(R.id.bookCategoryTextView)
        val electronicBadge: TextView = itemView.findViewById(R.id.electronicBadge)
        val availableBadge: TextView = itemView.findViewById(R.id.availableBadge)
        val detailsButton: MaterialButton = itemView.findViewById(R.id.detailsButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book_card, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]

        holder.titleTextView.text = book.title
        holder.authorTextView.text = "Автор: ${book.author}"
        holder.categoryTextView.text = "Раздел: ${book.category}"

        // Метки
        holder.electronicBadge.visibility = if (book.isElectronic) View.VISIBLE else View.GONE
        holder.availableBadge.visibility = View.VISIBLE // В учебном проекте всегда доступна

        // Кнопка "Подробнее"
        holder.detailsButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, BookDetailsActivity::class.java)
            intent.putExtra("book_title", book.title)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = books.size

    fun updateData(newBooks: List<Book>) {
        this.books = newBooks
        notifyDataSetChanged()
    }
}