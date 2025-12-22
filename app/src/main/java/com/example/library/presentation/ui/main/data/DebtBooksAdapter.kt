package com.example.library.presentation.ui.main.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.library.R

class DebtBooksAdapter(private var debts: List<DebtBook>) :
    RecyclerView.Adapter<DebtBooksAdapter.DebtViewHolder>() {

    inner class DebtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val daysTextView: TextView = itemView.findViewById(R.id.daysTextView)
        val penaltyTextView: TextView = itemView.findViewById(R.id.penaltyTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_debt_book, parent, false)
        return DebtViewHolder(view)
    }

    override fun onBindViewHolder(holder: DebtViewHolder, position: Int) {
        val debt = debts[position]

        holder.titleTextView.text = debt.title
        holder.daysTextView.text = "Просрочено: ${debt.daysOverdue} дней"
        holder.penaltyTextView.text = "Штраф: ${debt.penalty} ₽"

        holder.itemView.setOnClickListener {
            Toast.makeText(it.context, "Подробнее: ${debt.title}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = debts.size

    fun updateData(newDebts: List<DebtBook>) {
        this.debts = newDebts
        notifyDataSetChanged()
    }
}