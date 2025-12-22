package com.example.library.presentation.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.library.R
import com.example.library.presentation.ui.main.BD.ExternalBook

class SearchResultAdapter(
    private var books: List<ExternalBook>,
    private val onBookClick: (ExternalBook) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.bookTitle)
        val author: TextView = itemView.findViewById(R.id.bookAuthor)
        val isbn: TextView = itemView.findViewById(R.id.bookIsbn)
        val cover: ImageView = itemView.findViewById(R.id.bookCover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.title.text = book.title
        holder.author.text = book.author
        holder.isbn.text = book.isbn?.let { "ISBN: $it" } ?: "ISBN: —"

        // Загружаем обложку (можно через Glide, но для учебы — placeholder)
        holder.cover.setImageResource(R.drawable.ic_placeholder_book)

        holder.itemView.setOnClickListener {
            onBookClick(book)
        }
    }

    override fun getItemCount() = books.size

    fun updateData(newBooks: List<ExternalBook>) {
        this.books = newBooks
        notifyDataSetChanged()
    }
}