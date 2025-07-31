package farsi.toenglish.translation.adapters

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import farsi.toenglish.translation.R
import farsi.toenglish.translation.database.AppDatabase
import farsi.toenglish.translation.database.Bookmark
import farsi.toenglish.translation.database.History
import farsi.toenglish.translation.utils.Consts
import farsi.toenglish.translation.utils.CustomMessage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistoryAdapter(context: Context, @LayoutRes resource: Int, objects: List<History>) :
    ArrayAdapter<History>(context, resource, objects) {
    private lateinit var addBookmark: Job
    private lateinit var deleteHistory: Job
    private var languagesNamesRes: Array<out String>? = null




    private class ViewHolder {
        lateinit var root: ConstraintLayout
        lateinit var tvFromLang: TextView
        lateinit var tvToLang: TextView
        lateinit var tvFromText: TextView
        lateinit var tvToText: TextView
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
        val translationHistory: History? = getItem(position)
        translationHistory?.let {
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

                holder.tvFromLang.text = convertedFromLang
                holder.tvToLang.text = convertedToLang
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

        }

        val finalConvertView: View? = view
        holder.root.setOnCreateContextMenuListener { menu, _, _ ->
            menu.add(context.getString(R.string.new_conversation))
                .setOnMenuItemClickListener {
                    translationHistory?.let { tr ->
                        val b = Bundle()
                        b.putString(Consts.langFrom, tr.fromLang)
                        b.putString(Consts.langTo, tr.toLang)
                        b.putInt(Consts.langFromPosition, tr.fromLangPosition)
                        b.putInt(Consts.langToPosition, tr.toLangPosition)
                        b.putString(Consts.textFrom, tr.fromText)
                        b.putString(Consts.textTo, tr.toText)

                        finalConvertView?.findNavController()?.navigate(
                            R.id.action_historyFragment_to_conversationFragment,
                            b
                        )

                    }
                    true
                }
            menu.add(context.getString(R.string.new_translate)).setOnMenuItemClickListener {
                val b = Bundle()
                b.putString(Consts.langFrom, "${translationHistory?.fromLang}")
                b.putString(Consts.langTo, "${translationHistory?.toLang}")
                b.putInt(Consts.langFromPosition, translationHistory?.fromLangPosition?:Consts.DEFAULT_LANG_POS_FROM)
                b.putInt(Consts.langToPosition, translationHistory?.toLangPosition?:Consts.DEFAULT_LANG_POS_TO)
                b.putString(Consts.textFrom, translationHistory?.fromText?:"")
                b.putString(Consts.textTo, translationHistory?.toText?:"")
                finalConvertView?.let {
                    finalConvertView.findNavController().navigate(
                        R.id.action_historyFragment_to_translateFragment,
                        b
                    )
                }
                true
            }
        }
        holder.ibDelete.setOnClickListener {
            val db = AppDatabase.instance(context.applicationContext)
            deleteHistory = CoroutineScope(Dispatchers.IO).launch {

                db.historyDao().deleteHistoryTranslation(translationHistory!!)
                withContext(Dispatchers.Main) {
                    remove(translationHistory)
                    notifyDataSetChanged()
                    CustomMessage.showToast(context.getString(R.string.translation_deleted), context)
                }
            }

        }
        holder.ibBookmark.setOnClickListener {
            val db = AppDatabase.instance(context.applicationContext)

            val translationBookmark = Bookmark(
                getItem(position)!!.fromLang,
                getItem(position)!!.toLang,
                getItem(position)!!.fromLangPosition,
                getItem(position)!!.toLangPosition,
                holder.tvFromText.text.toString(),
                holder.tvToText.text.toString(),
                System.currentTimeMillis().toString()
            )
            addBookmark = CoroutineScope(Dispatchers.IO).launch {
                val translationBookmarkList: List<Bookmark> =
                    db.bookmarkDao().getAllBookmarkTranslationOutside()
                for (i in translationBookmarkList.indices) {
                    if (translationBookmark.fromLang
                            .equals(translationBookmarkList[i].fromLang) && translationBookmark.toLang
                            .equals(
                                translationBookmarkList[i].toLang
                            ) && translationBookmark.fromText.equals(
                            translationBookmarkList[i].fromText
                        ) && translationBookmark.toText.equals(
                            translationBookmarkList[i].toText
                        )
                    ) {
                        db.bookmarkDao().deleteBookmarkTranslation(translationBookmarkList[i])
                    }
                }
                db.bookmarkDao().addBookmarkTranslation(translationBookmark)

                withContext(Dispatchers.Main) {
                    CustomMessage.showToast(context.getString(R.string.bookmark_added), context)
                }
            }
        }
        return view!!
    }

    override fun clear() {

        if (::deleteHistory.isInitialized && !deleteHistory.isCompleted)
            deleteHistory.cancel()
        if (::addBookmark.isInitialized && !addBookmark.isCompleted)
            addBookmark.cancel()
    }
}