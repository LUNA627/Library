package com.example.library.presentation.ui.main.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.library.R
import com.google.android.material.button.MaterialButton

class ActiveBookAdapter(private var books: List<ActiveBook>) :
    RecyclerView.Adapter<ActiveBookAdapter.BookViewHolder>() {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coverImageView: ImageView = itemView.findViewById(R.id.bookCoverImageView)
        val titleTextView: TextView = itemView.findViewById(R.id.bookTitleTextView)
        val issueDateTextView: TextView = itemView.findViewById(R.id.issueDateTextView)
        val returnDateTextView: TextView = itemView.findViewById(R.id.returnDateTextView)
        val statusBadge: TextView = itemView.findViewById(R.id.statusBadge)
        val actionButton: MaterialButton = itemView.findViewById(R.id.actionButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_active_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]

        holder.titleTextView.text = book.title
        holder.issueDateTextView.text = "Выдано: ${book.issueDate}"
        holder.returnDateTextView.text = if (book.isElectronic) "" else "Вернуть до: ${book.returnDate}"
        holder.statusBadge.text = book.status


        val buttonText = if (book.isElectronic) "ЧИТАТЬ" else "ПРОДЛИТЬ"
        holder.actionButton.text = buttonText

        holder.actionButton.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Кнопка $buttonText нажата", Toast.LENGTH_SHORT).show()
        }

        holder.coverImageView.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Подробности: ${book.title}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = books.size

    fun updateData(newBooks: List<ActiveBook>) {
        this.books = newBooks
        notifyDataSetChanged()
    }
}