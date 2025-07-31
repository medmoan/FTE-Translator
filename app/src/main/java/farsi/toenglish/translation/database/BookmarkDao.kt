package farsi.toenglish.translation.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmark")
    fun getAllBookmarkTranslation(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmark")
    suspend fun getAllBookmarkTranslationOutside(): List<Bookmark>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmarkTranslation(bookmark: Bookmark)

    @Delete
    suspend fun deleteBookmarkTranslation(bookmark: Bookmark)
}
