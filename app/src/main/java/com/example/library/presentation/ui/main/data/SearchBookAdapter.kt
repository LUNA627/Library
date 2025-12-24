package com.example.library.presentation.ui.main.data

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.library.R
import com.example.library.presentation.ui.main.AddEditBookActivity
import com.example.library.presentation.ui.main.BD.ExternalBook

class SearchBookAdapter(
    private var books: List<ExternalBook> = emptyList(),
    private val onItemClick: (ExternalBook) -> Unit // ← добавь это
) : RecyclerView.Adapter<SearchBookAdapter.ViewHolder>() {

    fun updateData(newBooks: List<ExternalBook>) {
        books = newBooks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.title.text = book.title
        holder.author.text = "Автор: ${book.author}"

        holder.itemView.setOnClickListener {
            onItemClick(book) // ← вызов лямбды
        }
    }

    override fun getItemCount() = books.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.bookTitle)
        val author: TextView = itemView.findViewById(R.id.bookAuthor)
    }
}