package com.example.library.presentation.ui.main.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.library.R
import com.example.library.presentation.ui.main.BD.User

class UserAdapter(
    private var users: List<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.userName)
        val email: TextView = itemView.findViewById(R.id.userEmail)
        val debtorBadge: TextView = itemView.findViewById(R.id.debtorBadge)
        val roleTextView: TextView = itemView.findViewById(R.id.userRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.name.text = user.fullName
        holder.email.text = user.email
        val roleName = when (user.role) {
            "student" -> "Студент"
            "teacher" -> "Преподаватель"
            "librarian" -> "Библиотекарь"
            else -> "Неизвестный"
        }
        holder.roleTextView.text = roleName
        holder.debtorBadge.visibility = if (user.isBlocked) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            onItemClick(user)
        }
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}