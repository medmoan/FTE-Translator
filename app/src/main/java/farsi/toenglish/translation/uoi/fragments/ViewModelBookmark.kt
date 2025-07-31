package farsi.toenglish.translation.uoi.fragments

import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

import farsi.toenglish.translation.database.AppDatabase
import farsi.toenglish.translation.database.Bookmark


class ViewModelBookmark(private val app: Application): AndroidViewModel(app){



    fun getBookmark(): LiveData<List<Bookmark>>{
        val db = AppDatabase.instance(app.applicationContext)
        val bookmarkDao = db.bookmarkDao()
        return bookmarkDao.getAllBookmarkTranslation()
    }
}