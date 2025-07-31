package farsi.toenglish.translation.adapters

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController

import kotlinx.coroutines.*


import farsi.toenglish.translation.R
import farsi.toenglish.translation.database.AppDatabase
import farsi.toenglish.translation.database.Bookmark
import farsi.toenglish.translation.utils.Consts
import farsi.toenglish.translation.utils.CustomMessage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BookmarkAdapter(context: Context, @LayoutRes resource: Int, objects: List<Bookmark>) :
    ArrayAdapter<Bookmark>(context, resource, objects) {
    /**
     * Holds variables in a View
     */
    private var languagesNamesRes: Array<out String>? = null
    private lateinit var deleteBookmark:Job

    private class ViewHolder {
        lateinit var root: ConstraintLayout
        lateinit var tvFromText: TextView
        lateinit var tvToText: TextView
        lateinit var tvFromLang: TextView
        lateinit var tvToLang: TextView
        lateinit var tvDate: TextView
        lateinit var ibDelete: ImageButton
        lateinit var ibBookmark: ImageButton
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {


        //ViewHolder object
        var view = convertView
        val holder: ViewHolder
        if (view == null) {
            val inflater: LayoutInflater = LayoutInflater.from(parent.context)
            view = inflater.inflate(R.layout.item_history, parent, false)
            holder = ViewHolder()

            holder.tvFromLang = view.findViewById(R.id.tv_from_lang)
            holder.tvToLang = view.findViewById(R.id.tv_to_lang)

            holder.tvFromText = view.findViewById(R.id.tv_from_text)
            holder.tvToText = view.findViewById(R.id.tv_to_text)

            holder.tvDate = view.findViewById(R.id.tv_date)
            holder.ibDelete = view.findViewById(R.id.ib_delete)
            holder.ibBookmark = view.findViewById(R.id.ib_bookmark)
            holder.root = view.findViewById(R.id.rootelement)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        val translationBookmark: Bookmark? = getItem(position)
        translationBookmark?.let {
            val isFarsi = context.resources.getBoolean(R.bool.is_farsi)
            if (isFarsi){
                var conf: Configuration = context.resources.configuration
                conf = Configuration(conf)
                conf.setLocale(Locale(Consts.LANGUAGE.fa))
                val localizedContext = context.createConfigurationContext(conf)
                val res = localizedContext.resources
                if (languagesNamesRes == null){
                    languagesNamesRes = res.getStringArray(R.array.language_names)
                }
                val convertedFromLang = languagesNamesRes!![it.fromLangPosition]
                val convertedToLang = languagesNamesRes!![it.toLangPosition]

                holder.tvFromLang.text = convertedFromLang
                holder.tvToLang.text = convertedToLang
                val locale = Locale(Consts.LANGUAGE.fa)

                var currentDateTime = 0L
                try {
                    currentDateTime = it.date.toLong()
                } catch(ex: NumberFormatException){ // handle your exception
                    ex.printStackTrace()
                }

                if (currentDateTime == 0L){
                    return@let
                }

                //creating Date from millisecond
                val currentDate = Date(currentDateTime)

                holder.tvDate.text =
                    SimpleDateFormat.getDateInstance(
                        SimpleDateFormat.FULL, locale).format(currentDate)
            }
            else {
                holder.tvFromLang.text = it.fromLang
                holder.tvToLang.text = it.toLang
                val locale = Locale(Consts.LANGUAGE.en)
                var currentDateTime = 0L
                try {
                    currentDateTime = it.date.toLong()
                } catch(ex: NumberFormatException){ // handle your exception
                    ex.printStackTrace()
                }

                if (currentDateTime == 0L){
                    return@let
                }

                val cal = Calendar.getInstance(locale)
                cal.timeInMillis = currentDateTime
                holder.tvDate.text = cal.time.toString()
            }



            holder.tvFromText.text = it.fromText
            holder.tvToText.text = it.toText
            holder.ibBookmark.visibility = View.INVISIBLE
        }

        val finalConvertView: View? = view
        holder.root.setOnCreateContextMenuListener { menu, _, _ ->
            menu.add(context.getString(R.string.new_conversation))
                .setOnMenuItemClickListener {
                    translationBookmark?.let {
                        val b = Bundle()
                        b.putString(Consts.langFrom, it.fromLang)
                        b.putString(Consts.langTo, it.toLang)
                        b.putInt(Consts.langFromPosition, it.fromLangPosition)
                        b.putInt(Consts.langToPosition, it.toLangPosition)
                        b.putString(Consts.textFrom, it.fromText)
                        b.putString(Consts.textTo, it.toText)

                        finalConvertView?.let {
                            finalConvertView.findNavController().navigate(
                                R.id.action_bookmarkFragment_to_conversationFragment,
                                b
                            )
                        }
                    }

                    true
                }
            menu.add(context.getString(R.string.new_translate)).setOnMenuItemClickListener {
                translationBookmark?.let {
                    val b = Bundle()
                    b.putString(Consts.langFrom, it.fromLang)
                    b.putString(Consts.langTo, it.toLang)
                    b.putInt(Consts.langFromPosition, it.fromLangPosition)
                    b.putInt(Consts.langToPosition, it.toLangPosition)
                    b.putString(Consts.textFrom, it.fromText)
                    b.putString(Consts.textTo, it.toText)
                    finalConvertView?.findNavController()?.navigate(
                        R.id.action_bookmarkFragment_to_translateFragment,
                        b
                    )
                }
                true
            }
        }
        holder.ibDelete.setOnClickListener {
            val db = AppDatabase.instance(context.applicationContext)
            deleteBookmark = CoroutineScope(Dispatchers.IO).launch {

                db.bookmarkDao().deleteBookmarkTranslation(translationBookmark!!)
                withContext(Dispatchers.Main) {
                    remove(translationBookmark)
                    notifyDataSetChanged()
                    CustomMessage.showToast(context.getString(R.string.bookmak_deleted), context)
                }
            }

        }
        return view!!
    }
    private fun getLocale(): String{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            val currentLocaleApp = context.getSystemService(LocaleManager::class.java).applicationLocales
            "$currentLocaleApp"
        } else {
            val currentLocaleApp = AppCompatDelegate.getApplicationLocales()
            "$currentLocaleApp"
        }
    }
    override fun clear() {

        if (::deleteBookmark.isInitialized && !deleteBookmark.isCompleted)
            deleteBookmark.cancel()
    }
}