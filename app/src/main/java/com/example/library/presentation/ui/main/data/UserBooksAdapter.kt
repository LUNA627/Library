package com.example.library.presentation.ui.main.data

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.library.R

class UserBooksAdapter(
    private val context: Context, // ← добавили контекст
    private var books: List<UserBookItem>,
    private val onExtendClick: (UserBookItem) -> Unit,
    private val onReturnClick: (UserBookItem) -> Unit // ← новое!
) : RecyclerView.Adapter<UserBooksAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.bookTitle)
        val info: TextView = itemView.findViewById(R.id.bookInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.title.text = book.title
        holder.info.text = if (book.returnInfo == "Электронная") {
            "Электронная книга"
        } else {
            "Вернуть до: ${book.returnInfo}"
        }

        // Продление — короткий клик
        if (book.canExtend) {
            holder.itemView.alpha = 1f
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.purple_700))
            holder.itemView.setOnClickListener {
                onExtendClick(book)
            }
        } else {
            holder.itemView.alpha = 0.6f
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.gray_dark))
            holder.itemView.isClickable = false
        }

        // 🔥 ВОЗВРАТ — ДОЛГОЕ НАЖАТИЕ (даже если нельзя продлить!)
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(context)
                .setTitle("Вернуть книгу?")
                .setMessage("Вы уверены, что хотите вернуть «${book.title}»?")
                .setPositiveButton("Да") { _, _ ->
                    onReturnClick(book)
                }
                .setNegativeButton("Нет", null)
                .show()
            true
        }
    }

    override fun getItemCount() = books.size

    fun updateData(newBooks: List<UserBookItem>) {
        books = newBooks
        notifyDataSetChanged()
    }
}