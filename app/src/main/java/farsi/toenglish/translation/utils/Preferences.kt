package farsi.toenglish.translation.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.Preference

import kotlinx.coroutines.flow.first
import kotlin.coroutines.cancellation.CancellationException

class Preferences private constructor(val context: Context) {
    private val Context.prefs: DataStore<Preferences> by preferencesDataStore(Consts.APP_PREFERENCES)

    companion object{
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: farsi.toenglish.translation.utils.Preferences? = null
        fun instance(context: Context): farsi.toenglish.translation.utils.Preferences{
            if (instance == null){
                synchronized(farsi.toenglish.translation.utils.Preferences::class.java){
                    if (instance == null){
                        instance = Preferences(context)
                    }
                }
            }
            return instance!!
        }
    }


    suspend fun save(key : String, value : Any) {
        try {
            when(value) {
                is String -> {
                    stringPreferencesKey(key).let { pKey ->
                        context.prefs.edit { pref ->
                            pref[pKey] = value
                        }
                    }
                }
                is Int -> {
                    intPreferencesKey(key).let { pKey ->
                        context.prefs.edit { pref ->
                            pref[pKey] = value
                        }
                    }
                }
                is Long -> {
                    longPreferencesKey(key).let { pKey ->
                        context.prefs.edit { pref ->
                            pref[pKey] = value
                        }
                    }
                }
                is Boolean -> {
                    booleanPreferencesKey(key).let { pKey ->
                        context.prefs.edit { pref ->
                            pref[pKey] = value
                        }
                    }
                }
                else -> Unit
            }
        }
        catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }
    suspend fun getInt(key : String, defaultValue: Int): Int {
        return try {
            val dataStoreKey = intPreferencesKey(key)
            val preferences = context.prefs.data.first()
            preferences[dataStoreKey]?: defaultValue

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            defaultValue
        }
    }
    suspend fun getString(key : String, defaultValue: String): String {
        return try {
            val dataStoreKey = stringPreferencesKey(key)
            val preferences = context.prefs.data.first()
            preferences[dataStoreKey]?: defaultValue

        } catch (e:Exception) {
            if(e is CancellationException) throw e
            e.printStackTrace()
            defaultValue
        }
    }
    suspend fun getLong(key : String, defaultValue: Long): Long {
        return try {
            val dataStoreKey = longPreferencesKey(key)
            val preferences = context.prefs.data.first()
            preferences[dataStoreKey]?: defaultValue
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            defaultValue
        }
    }
    suspend fun getBoolean(key : String, defaultValue: Boolean): Boolean {
        return try {
            val dataStoreKey = booleanPreferencesKey(key)
            val preferences = context.prefs.data.first()
            preferences[dataStoreKey]?: defaultValue
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            defaultValue
        }
    }
}