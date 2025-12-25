package com.example.library.presentation.ui.main.data

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.library.R
import com.example.library.presentation.ui.main.BookDetailsActivity
import com.example.library.presentation.ui.main.MainActivity
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson

class BookAdapter(
    private val context: Context,
    private var books: List<DisplayBook>
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

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

        holder.electronicBadge.visibility = if (book.isElectronic) View.VISIBLE else View.GONE
        holder.availableBadge.visibility = View.VISIBLE

        Glide.with(holder.coverImageView.context)
            .load(book.imageUrl)
            .placeholder(R.drawable.ic_placeholder_book)
            .fitCenter()
            .into(holder.coverImageView)

        Log.d("BOOK_IMAGE", "URL: ${book.imageUrl}")


        holder.detailsButton.text = "Добавить"
        holder.detailsButton.setOnClickListener {
            // Обновляем состояние кнопки
            if (book.isAdded) {
                holder.detailsButton.text = "Добавлено!"
                holder.detailsButton.isEnabled = false
            }
            else {
                holder.detailsButton.text = "Добавить"
                holder.detailsButton.isEnabled = true
                holder.detailsButton.setOnClickListener {
                    if (context is MainActivity) {
                        context.addBookToUserProfile(book) { success ->
                            if (success) {
                                book.isAdded = true
                                holder.detailsButton.text = "Добавлено!"
                                holder.detailsButton.isEnabled = false
                            }
                        }
                    }
                }
            }
        }
    }



    fun updateData(newBooks: List<DisplayBook>) {
        this.books = newBooks
        notifyDataSetChanged()
    }

    override fun getItemCount() = books.size

}