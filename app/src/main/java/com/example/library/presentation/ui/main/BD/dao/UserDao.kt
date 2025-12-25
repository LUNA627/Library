package com.example.library.presentation.ui.main.BD.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.library.presentation.ui.main.BD.User

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM user WHERE role = 'student' OR role = 'teacher'")
    suspend fun getAllReaders(): List<User>

    @Query("SELECT * FROM user WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT COUNT(*) FROM user WHERE isBlocked = 1")
    suspend fun getBlockedUsersCount(): Int

    @Update
    suspend fun updateUser(user: User)

}