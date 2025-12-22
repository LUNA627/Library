package com.example.library.presentation.ui.main.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.library.R

class TakenBooksAdapter(private var books: List<TakenBook>) :
    RecyclerView.Adapter<TakenBooksAdapter.BookViewHolder>() {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coverImageView: ImageView = itemView.findViewById(R.id.bookCoverImageView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val statusBadge: TextView = itemView.findViewById(R.id.statusBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_taken_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]

        holder.titleTextView.text = book.title
        holder.coverImageView.setImageResource(book.coverRes)

        // Цвет бейджа по статусу
        when (book.status) {
            "active" -> {
                holder.statusBadge.text = "Активна"
                holder.statusBadge.setBackgroundResource(R.drawable.badge_blue)
            }
            "returned" -> {
                holder.statusBadge.text = "Прочитана"
                holder.statusBadge.setBackgroundResource(R.drawable.badge_green)
            }
            "overdue" -> {
                holder.statusBadge.text = "Просрочена"
                holder.statusBadge.setBackgroundResource(R.drawable.badge_red)
            }
        }

        // Клик — подробности
        holder.itemView.setOnClickListener {
            Toast.makeText(it.context, "Подробнее: ${book.title}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = books.size

    fun updateData(newBooks: List<TakenBook>) {
        this.books = newBooks
        notifyDataSetChanged()
    }
}