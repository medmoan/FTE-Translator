package farsi.toenglish.translation.uoi.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import farsi.toenglish.translation.databinding.FragmentDictionaryBinding
import farsi.toenglish.translation.uoi.activities.SharedViewModel
import farsi.toenglish.translation.R
import farsi.toenglish.translation.utils.FarsiSupport
import farsi.toenglish.translation.utils.UnityAdsUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

class DictionaryFragment: Fragment() {
    private var _binding: FragmentDictionaryBinding? = null
    private val binding get() = _binding!!
    private var ads: UnityAdsUtil? = null
    private lateinit var sharedViewModel: SharedViewModel
    private var sqliteDB: SQLiteDatabase? = null
    private var enablePopup = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                displayInterstitial(ads){
                    findNavController().navigateUp()
                }
            }
        }


        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            callback)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        _binding = FragmentDictionaryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        sharedViewModel.consentPermit.observe(viewLifecycleOwner) { loadAds ->
            if (loadAds){
                if (ads == null){
                    ads = UnityAdsUtil(requireContext()).apply {
                        loadInterstitial()
                    }

                }
            }

        }
        if (!File(
                "/data/data/" + requireContext().packageName
                        + "/database.sqlite"
            ).exists()
        ) {
            try {
                val out = FileOutputStream(
                    ("data/data/"
                            + requireContext().packageName + "/database.sqlite")
                )
                val `in` = requireContext().assets.open("databases/dic.db")
                val buffer = ByteArray(1024)
                var readBytes: Int
                while ((`in`.read(buffer).also { readBytes = it }) != -1) out.write(
                    buffer,
                    0,
                    readBytes
                )
                `in`.close()
                out.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        sqliteDB = SQLiteDatabase.openOrCreateDatabase(
            "/data/data/" + requireContext().packageName + "/database.sqlite",
            null
        )
        binding.searchbox.threshold = 1
        binding.backImageView.setOnClickListener{
            displayInterstitial(ads){
                findNavController().navigateUp()
            }
        }
        binding.searchbox.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int,
                count: Int
            ) {
                if (s.length >= 1 && enablePopup) {

                    val cursor = sqliteDB!!.rawQuery(
                        "SELECT word FROM dictionary WHERE word LIKE '"
                                + s.toString().lowercase(Locale.getDefault())
                                + "%' LIMIT 10", null
                    )
                    if (cursor.count > 0) {
                        cursor.moveToFirst()
                        val word = ArrayList<String>()
                        while (!cursor.isAfterLast) {
                            word.add(cursor.getString(0))
                            cursor.moveToNext()
                        }
                        binding.searchbox.setAdapter(
                            DropDownAdapter(
                                requireContext(),
                                sqliteDB!!, word
                            )
                        )
                    } else {
                        binding.searchbox.setAdapter(
                            DropDownAdapter(
                                requireContext(),
                                sqliteDB!!, ArrayList<String>()
                            )
                        )
                    }
                } else {
                    binding.searchbox.setAdapter(
                        DropDownAdapter(
                            requireContext(), sqliteDB!!,
                            ArrayList<String>()
                        )
                    )
                }
                enablePopup = true
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    override fun onDestroyView() {
        sqliteDB?.close()
        sqliteDB = null
        ads?.destroyInterstitial()
        _binding = null
        super.onDestroyView()
    }
    inner class DropDownAdapter(
        context: Context, private val sqliteDB: SQLiteDatabase,
        private val data: java.util.ArrayList<String>
    ) : ArrayAdapter<String?>(context, R.layout.list_item_item, data as List<String?>) {
        @SuppressLint("ClickableViewAccessibility", "ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val inflater =
                requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val rowView = inflater.inflate(
                R.layout.list_item_item,
                parent, false
            ) as View
            val textView = rowView.findViewById<View>(R.id.item_text) as TextView
            textView.text = data[position]
            textView.setOnTouchListener { _, _ ->
                Thread {
                    val cursor = sqliteDB
                        .rawQuery(
                            "SELECT mean FROM dictionary WHERE word='"
                                    + data[position]
                                .lowercase(Locale.getDefault())
                                    + "'", null
                        )
                    cursor.moveToFirst()
                    val wordMean = cursor.getString(0)
                    cursor.close()
                    requireActivity().runOnUiThread(Runnable {
                        val tv =
                            requireView().findViewById<View>(R.id.searchbox) as AutoCompleteTextView
                        val mean =
                            requireView().findViewById<View>(R.id.passage) as TextView
                        mean.typeface = Typeface.createFromAsset(
                            requireContext().assets,
                            "fonts/Nazanin.ttf"
                        )
                        mean.setText(FarsiSupport.Convert(wordMean))
                        tv.setText(data[position])
                        enablePopup = false
                        tv.dismissDropDown()
                    })
                }.start()
                true
            }
            return rowView
        }
    }
}