package com.example.library.presentation.ui.main.BD.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.library.presentation.ui.main.BD.ExternalBook
import com.example.library.presentation.ui.main.BD.LibraryBook
import com.example.library.presentation.ui.main.BD.Loan

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertExternalBook(book: ExternalBook)

    @Insert
    suspend fun insertLibraryBook(libraryBook: LibraryBook): Long

    @Query("SELECT * FROM library_book")
    suspend fun getAllLibraryBooks(): List<LibraryBook>

    @Query("SELECT * FROM external_book WHERE apiId = :apiId")
    suspend fun getExternalBookById(apiId: String): ExternalBook?


    @Query("DELETE FROM library_book WHERE bookId = :bookId")
    suspend fun deleteLibraryBook(bookId: Long)

    @Query("SELECT * FROM library_book WHERE externalBookId = :externalBookId")
    suspend fun getLibraryBookByExternalId(externalBookId: String): LibraryBook?


    @Query("SELECT * FROM library_book WHERE bookId = :bookId")
    suspend fun getLibraryBookById(bookId: Long): LibraryBook?

    @Query("SELECT COUNT(*) FROM library_book")
    suspend fun getTotalBooks(): Int

    @Query("SELECT COUNT(*) FROM loan WHERE status = 'active'")
    suspend fun getActiveLoans(): Int

    @Query("SELECT * FROM loan WHERE userId = :userId AND status = 'active'")
    suspend fun getActiveLoansByUser(userId: Long): List<Loan>

    @Query("SELECT * FROM loan WHERE userId = :userId AND status = 'returned'")
    suspend fun getReturnedLoansByUser(userId: Long): List<Loan>

    @Query("SELECT * FROM loan WHERE userId = :userId")
    suspend fun getAllLoansByUser(userId: Long): List<Loan>

    @Insert
    suspend fun insertLoan(loan: Loan): Long

    @Update
    suspend fun updateLibraryBook(libraryBook: LibraryBook)

    @Query("SELECT * FROM external_book")
    suspend fun getAllExternalBooks(): List<ExternalBook>

}