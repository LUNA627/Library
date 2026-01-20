package com.example.library.presentation.ui.main.BD

import com.example.library.presentation.ui.main.BD.dao.BookDao
import com.example.library.presentation.ui.main.BD.dao.UserDao

class BookRepository(
    private val bookDao: BookDao,
    private val api: GoogleBooksApi? = null
) {

    // Поиск книг через API
    suspend fun searchBooks(query: String): List<ExternalBook> {
        if (api == null) {
            return emptyList()
        }
        val response = api.searchBooks(query)
        return response.items?.map { item ->
            val isbn = item.volumeInfo.industryIdentifiers
                ?.find { it.type == "ISBN_13" || it.type == "ISBN_10" }
                ?.identifier

            val imageUrl = item.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
            val smallThumbnail = item.volumeInfo.imageLinks?.smallThumbnail?.replace("http://", "https://")

            ExternalBook(
                apiId = item.id,
                title = item.volumeInfo.title,
                author = item.volumeInfo.authors?.joinToString(", ") ?: "Unknown",
                description = item.volumeInfo.description,
                imageUrl = imageUrl,
                thumbnailUrl = smallThumbnail,
                isbn = isbn,
                categoryId = 1L
            )
        } ?: emptyList()
    }

    // Добавление книги в фонд
    suspend fun addLibraryBook(
        externalBook: ExternalBook,
        categoryId: Long,
        isElectronic: Boolean,
        copies: Int
    ) {
        // Сохраняем ExternalBook
        bookDao.insertExternalBook(externalBook)

        // Проверяем, есть ли уже такая книга
        val existingBook = bookDao.getLibraryBookByExternalId(externalBook.apiId)
        if (existingBook != null) {
            // Обновляем количество
            val updatedBook = existingBook.copy(
                copiesTotal = existingBook.copiesTotal + copies,
                copiesAvailable = existingBook.copiesAvailable + copies
            )
            bookDao.updateLibraryBook(updatedBook)
        } else {
            // Добавляем новую
            val libraryBook = LibraryBook(
                externalBookId = externalBook.apiId,
                categoryId = categoryId,
                isElectronic = isElectronic,
                copiesTotal = if (isElectronic) 1 else copies,
                copiesAvailable = if (isElectronic) 1 else copies
            )
            bookDao.insertLibraryBook(libraryBook)
        }
    }

    // Получить все книги в фонде
    suspend fun getAllLibraryBooks(): List<LibraryBook> = bookDao.getAllLibraryBooks()

    suspend fun getExternalBookById(apiId: String): ExternalBook? {
        return bookDao.getExternalBookById(apiId)
    }

    suspend fun updateLibraryBook(book: LibraryBook) {
        bookDao.updateLibraryBook(book)
    }

    suspend fun deleteLibraryBook(bookId: Long) {
        bookDao.deleteLibraryBook(bookId)
    }

    suspend fun saveExternalBook(book: ExternalBook) {
        bookDao.insertExternalBook(book)
    }
    suspend fun getTotalBooks(): Int = bookDao.getTotalBooks()
    suspend fun getActiveLoans(): Int = bookDao.getActiveLoans()
}