package farsi.toenglish.translation.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [History::class,Bookmark::class], version = 2, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object{
        @Volatile
        private var appDatabase: AppDatabase? = null
        fun instance(context: Context): AppDatabase {
            if (appDatabase == null) {
                synchronized (AppDatabase::class.java) {
                    if (appDatabase == null) {
                        appDatabase = Room.databaseBuilder(
                            context,
                            AppDatabase::class.java,
                            "translationApp"

                        )
                            .fallbackToDestructiveMigration()
                            .build()
                    }
                }
            }
            return appDatabase!!
        }
    }
}
