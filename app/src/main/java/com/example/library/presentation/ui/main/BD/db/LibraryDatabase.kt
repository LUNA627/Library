package com.example.library.presentation.ui.main.BD.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.library.presentation.ui.main.BD.ExternalBook
import com.example.library.presentation.ui.main.BD.LibraryBook
import com.example.library.presentation.ui.main.BD.Loan
import com.example.library.presentation.ui.main.BD.User
import com.example.library.presentation.ui.main.BD.dao.BookDao
import com.example.library.presentation.ui.main.BD.dao.UserDao

@Database(
    entities = [User::class, ExternalBook::class, LibraryBook::class, Loan::class],
    version = 1
)
abstract class LibraryDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun userDao(): UserDao
}