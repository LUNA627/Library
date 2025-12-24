package com.example.library.presentation.ui.main.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.library.R

class UserBooksAdapter(private var books: List<UserBookItem>) :
    RecyclerView.Adapter<UserBooksAdapter.ViewHolder>() {

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
    }

    override fun getItemCount() = books.size

    fun updateData(newBooks: List<UserBookItem>) {
        books = newBooks
        notifyDataSetChanged()
    }
}