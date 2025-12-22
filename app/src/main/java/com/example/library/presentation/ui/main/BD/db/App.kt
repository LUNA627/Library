package com.example.library.presentation.ui.main.BD.db

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class App : Application() {
    companion object {
        lateinit var database: LibraryDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            LibraryDatabase::class.java,
            "library.db"
        )
            .build()
    }
}

