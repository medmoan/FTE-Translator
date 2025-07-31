package pl.utkala.searchablespinner


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Insets
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import java.io.Serializable


class SearchableSpinnerDialog : DialogFragment(), SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    private var items: MutableList<Any?> = arrayListOf("")
    private var mListView: ListView? = null
    private var mSearchView: SearchView? = null
    private var mDismissText: String? = null
    private var mDialogTitle: String? = null
    private var mDialogBackground: Drawable? = null
    private var mDismissListener: DialogInterface.OnClickListener? = null
    private var mCustomAdapter: ArrayAdapter<*>? = null
    var onSearchableItemClick: OnSearchableItemClick<Any?>? = null

    companion object {
        @JvmStatic
        val CLICK_LISTENER = "click_listener"

        fun getInstance(items: MutableList<Any?>, dialogBackground: Drawable? = null, customAdapter: ArrayAdapter<*>? = null): SearchableSpinnerDialog {
            val dialog = SearchableSpinnerDialog()
            dialog.items = items
            dialog.mDialogBackground = dialogBackground
            dialog.mCustomAdapter = customAdapter
            return dialog
        }
    }
    @Suppress("DEPRECATION", "UNCHECKED_CAST")
    private fun <T : Serializable?> Bundle.getSerializableCompat(key: String, clazz: Class<T>): T {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) getSerializable(key, clazz)!! else (getSerializable(key) as T)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setStyle(STYLE_NO_TITLE, R.style.DialogTheme);
    }
    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (savedInstanceState != null) {
            onSearchableItemClick = savedInstanceState.getSerializableCompat(CLICK_LISTENER, OnSearchableItemClick::class.java) as OnSearchableItemClickType
        }

        val layoutInflater = LayoutInflater.from(activity)
        val rootView = layoutInflater.inflate(R.layout.d, null)

        val alertBuilder = AlertDialog.Builder(activity)



        setView(rootView)

        alertBuilder.setView(rootView)


//        val title = if (mDialogTitle.isNullOrBlank()) getString(R.string.search_dialog_title) else mDialogTitle
//        alertBuilder.setTitle(title)
//
//        val dismiss = if(mDismissText.isNullOrBlank()) getString(R.string.search_dialog_close) else mDismissText
//        alertBuilder.setPositiveButton(dismiss, mDismissListener)

        return alertBuilder.create()
    }

    private var listAdapter: ArrayAdapter<*>? = null

    private fun setView(rootView: View?) {
        if (rootView == null) return



        val titleTv = rootView.findViewById<TextView>(R.id.title)
        val closeBtn = rootView.findViewById<TextView>(R.id.close)
        val listView = rootView.findViewById<ListView>(R.id.listView)
        listAdapter = mCustomAdapter ?: ArrayAdapter(requireContext(), R.layout.simple_spinner_dropdown_item, items)
        mListView = listView
        mListView?.adapter = listAdapter
        mListView?.isTextFilterEnabled = true
        mListView?.setOnItemClickListener { _, _, position, _ ->
            onSearchableItemClick?.onSearchableItemClicked(mListView?.adapter?.getItem(position), position)
            dialog?.dismiss()
        }
        val searchView = rootView.findViewById<SearchView>(R.id.searchView)
        mSearchView = searchView
        mSearchView?.setOnQueryTextListener(this)
        mSearchView?.setOnCloseListener(this)
        mSearchView?.clearFocus()
        val title = if (mDialogTitle.isNullOrBlank()) getString(R.string.search_dialog_title) else mDialogTitle
        titleTv.text = title
        titleTv.setTextColor(fetchPrimaryColor())

        val dismiss = if(mDismissText.isNullOrBlank()) getString(R.string.search_dialog_close) else mDismissText
        closeBtn.text = dismiss

        closeBtn.setOnClickListener {
            dismiss()
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        mSearchView?.clearFocus()
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query.isNullOrBlank()) {
            (mListView?.adapter as ArrayAdapter<*>).filter.filter(null)
        } else {
            (mListView?.adapter as ArrayAdapter<*>).filter.filter(query)
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(CLICK_LISTENER, onSearchableItemClick)
        super.onSaveInstanceState(outState)
    }

    override fun onClose(): Boolean {
        return false
    }

    override fun onPause() {
        onQueryTextChange("")
        super.onPause()
        dismiss()
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (mDialogBackground != null) {
            dialog?.window?.setBackgroundDrawable(mDialogBackground)
        }
        try {
            dialog?.apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog?.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setBackgroundDrawableResource(R.drawable.back_dialog_spinner)
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED)
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
            }
        }
            //dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        catch (exception: Exception){
            exception.printStackTrace()
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            dialog?.window?.setDecorFitsSystemWindows(false)
//            requireView().setOnApplyWindowInsetsListener { v, insets ->
//                Log.d("dialog", "onCreateView: ")
//                val imeInsets: Insets = insets.getInsets(WindowInsets.Type.ime())
//                v.setPadding(0, 0, 0, imeInsets.bottom)
//                insets
//            }
//        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun setDismissText(closeText: String?) {
        mDismissText = closeText
    }


    fun setDismissText(closeText: String?, listener: DialogInterface.OnClickListener) {
        mDismissText = closeText
        mDismissListener = listener
    }


    fun setTitle(dialogTitle: String?) {
        mDialogTitle = dialogTitle
    }
    @SuppressLint("DiscouragedApi")
    private fun fetchPrimaryColor(): Int {
        val typedValue = TypedValue()
        val colorAttr: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.R.attr.colorPrimary
        } else {
            //Get colorAccent defined for AppCompat
            requireContext().resources.getIdentifier("colorPrimary", "attr", requireContext().packageName)
        }
        val a: TypedArray =
            requireContext().obtainStyledAttributes(typedValue.data, intArrayOf(colorAttr))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }
}

typealias OnSearchableItemClickType = OnSearchableItemClick<Any?>