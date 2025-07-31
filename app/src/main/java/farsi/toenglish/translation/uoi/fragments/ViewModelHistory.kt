package farsi.toenglish.translation.uoi.fragments

import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

import farsi.toenglish.translation.database.AppDatabase

import farsi.toenglish.translation.database.History

class ViewModelHistory(private val app: Application): AndroidViewModel(app){




    fun getHistory(): LiveData<List<History>>{
        val db = AppDatabase.instance(app.applicationContext)
        val historyDao = db.historyDao()
        return historyDao.getAllHistoryTranslation()
    }

}