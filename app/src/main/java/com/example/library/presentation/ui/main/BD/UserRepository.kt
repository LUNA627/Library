package com.example.library.presentation.ui.main.BD

import com.example.library.presentation.ui.main.BD.dao.UserDao


class UserRepository(
    private val userDao: UserDao
) {
    suspend fun getBlockedUsersCount(): Int = userDao.getBlockedUsersCount()
}