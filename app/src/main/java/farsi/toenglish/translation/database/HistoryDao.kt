package farsi.toenglish.translation.database

import androidx.lifecycle.LiveData

import androidx.room.*


@Dao
interface HistoryDao {

    @Query("SELECT * FROM history")
    fun getAllHistoryTranslation(): LiveData<List<History>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHistoryTranslation(history: History)

    @Delete
    suspend fun deleteHistoryTranslation(history: History)
}