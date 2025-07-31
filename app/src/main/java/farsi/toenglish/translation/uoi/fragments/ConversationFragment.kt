package farsi.toenglish.translation.uoi.fragments


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.database.DataSetObserver
import android.os.*
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import farsi.toenglish.translation.database.AppDatabase
import farsi.toenglish.translation.database.History
import farsi.toenglish.translation.databinding.FragmentConversationBinding
import farsi.toenglish.translation.modules.ChatMess
import farsi.toenglish.translation.uoi.activities.SharedViewModel
import farsi.toenglish.translation.utils.*
import farsi.toenglish.translation.R
import farsi.toenglish.translation.adapters.ChatArrayAdapter
import farsi.toenglish.translation.data.Resource
import farsi.toenglish.translation.uoi.fragments.viemodels.ConversationViewModel
import farsi.toenglish.translation.utils.Consts
import pl.utkala.searchablespinner.OnSearchableItemClick
import pl.utkala.searchablespinner.StringHintArrayAdapter
import java.io.IOException
import java.util.*
import kotlin.coroutines.cancellation.CancellationException

class ConversationFragment : Fragment() {
    private var _binding: FragmentConversationBinding? = null
    private var ads: UnityAdsUtil? = null
    private val binding get() = _binding!!
    private var tts: TextToSpeech? = null
    private var mLanguageCodeFrom = Consts.LANGUAGE.en
    private var mLanguageCodeTo = Consts.LANGUAGE.en
    private var languagesNamesRes: Array<out String>? = null
    private var chatArrayAdapter: ChatArrayAdapter? = null

    private lateinit var sharedViewModel: SharedViewModel
    private val conversationViewModel by viewModels<ConversationViewModel>()


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // PERMISSION GRANTED
            val b = Bundle()
            b.putString(Consts.TRANSLATEMODE, Consts.CONVERSATIONTRANSLATE)
            findNavController().navigate(R.id.action_conversationFragment_to_imageTranslationFragment,
                b)
        }
        else {
            CustomMessage.showToast(getString(R.string.permisionexpl), requireContext())
        }
    }
    private val speechToTextResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && null != result.data) {

                val data: Intent = result.data!!
                val matchTextDialog = Dialog(requireContext())
                matchTextDialog.setContentView(R.layout.dialog_matches_frag)
                matchTextDialog.setTitle(getString(R.string.select_matching_text))
                val textlist = matchTextDialog.findViewById<View>(R.id.list) as ListView
                val matchesText: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
                val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    matchesText
                )
                textlist.adapter = adapter
                textlist.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, position, _ ->
                        val chatInput = matchesText[position]
                        matchTextDialog.dismiss()
                        binding.textInput.setText(chatInput)
                    }
                matchTextDialog.show()
            }
        }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                displayInterstitial(ads){
                    findNavController().navigateUp()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
       _binding = FragmentConversationBinding.inflate(layoutInflater, container, false)

        binding.imageSend.setOnClickListener {
            val animation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.touch_release)
            binding.imageSend.startAnimation(animation)
            if (requireActivity().currentFocus != null) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.textInput.windowToken, 0)
            }
            val chatInput = binding.textInput.text.toString().trim()

            if (chatInput.isEmpty()) {
                return@setOnClickListener
            }
            binding.progress.visibility = View.VISIBLE
            binding.imageSend.isEnabled = false
            conversationViewModel.translate(
                chatInput,
                mLanguageCodeFrom,
                mLanguageCodeTo
            )
        }
        binding.imgMic.setOnClickListener{
            val animation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.touch_release)
            binding.imgMic.startAnimation(animation)
            promptSpeechInput()
        }
        //  TEXT TO SPEECH
        binding.listChatView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val chatMess = chatArrayAdapter?.getItem(position)
                chatMess?.let {
                    speakOut(it.getmMessage(), it.getmLanguageCode())
                }
            }
        binding.homeBut.setOnClickListener {
            displayInterstitial(ads){
                findNavController().navigateUp()
            }
        }
        binding.clearChat.setOnClickListener {
            chatArrayAdapter!!.clear()
            chatArrayAdapter!!.notifyDataSetChanged()
        }
        binding.scan.setOnClickListener {
            val animation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.touch_release)
            binding.scan.startAnimation(animation)
            startCameraPermissionRequest()
        }
        return binding.root
    }
    private fun startCameraPermissionRequest() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }


    private fun setLanguagesSpinner(langFrom: String,langTo: String,langFromPosition: Int, langToPosition: Int) {
        val languagesNamesRes = requireActivity().resources.getStringArray(R.array.language_names)
        val languagesCodesRes = requireActivity().resources.getStringArray(R.array.languages_codes)
        val languagesNames: List<String> = ArrayList(listOf(*languagesNamesRes))
        val languagesCodes: List<String> = ArrayList(listOf(*languagesCodesRes))

        binding.spinnerLanguageFrom.adapter = StringHintArrayAdapter(requireContext(), R.layout.simple_list_item, languagesNames)
        binding.spinnerLanguageFrom.start()
        binding.spinnerLanguageFrom.onSearchableItemClick = object : OnSearchableItemClick<Any?> {
            override fun onSearchableItemClicked(item: Any?, position: Int) {
                if (position >= 0) {
                    binding.spinnerLanguageFrom.setLastSelectedItemIndex(position)
                    mLanguageCodeFrom = languagesCodes[position]
                } else {
                    binding.spinnerLanguageFrom.setSelection(Spinner.INVALID_POSITION)

                }
            }
        }
        binding.spinnerLanguageTo.adapter = StringHintArrayAdapter(requireContext(), R.layout.simple_list_item, languagesNames)
        binding.spinnerLanguageTo.start()
        binding.spinnerLanguageTo.onSearchableItemClick = object : OnSearchableItemClick<Any?> {
            override fun onSearchableItemClicked(item: Any?, position: Int) {
                if (position >= 0) {
                    binding.spinnerLanguageTo.setLastSelectedItemIndex(position)
                    mLanguageCodeTo = languagesCodes[position]
                } else {
                    binding.spinnerLanguageTo.setSelection(Spinner.INVALID_POSITION)

                }
            }
        }
        if (langFrom.isNotEmpty() && langTo.isNotEmpty()) {
            val isFarsi = requireActivity().resources.getBoolean(R.bool.is_farsi)
            if (isFarsi){

                val convertedFromLang = languagesNamesRes[langFromPosition]
                val convertedToLang = languagesNamesRes[langToPosition]

                binding.spinnerLanguageFrom.findSelectedItemIndexByName(convertedFromLang)
                binding.spinnerLanguageTo.findSelectedItemIndexByName(convertedToLang)
                mLanguageCodeFrom = languagesCodes[binding.spinnerLanguageFrom.getSelectedItemIndex()]
                mLanguageCodeTo = languagesCodes[binding.spinnerLanguageTo.getSelectedItemIndex()]
            }
            else {
                binding.spinnerLanguageFrom.findSelectedItemIndexByName(langFrom)
                binding.spinnerLanguageTo.findSelectedItemIndexByName(langTo)
                mLanguageCodeFrom = languagesCodes[binding.spinnerLanguageFrom.getSelectedItemIndex()]
                mLanguageCodeTo = languagesCodes[binding.spinnerLanguageTo.getSelectedItemIndex()]
            }
            return
        }
        val appPref = Preferences.instance(requireContext().applicationContext)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
            val lngFrom: Int = appPref.getInt(Consts.LANGFROM, Consts.DEFAULT_LANG_POS_FROM)
            val lngTo: Int = appPref.getInt(Consts.LANGTO, Consts.DEFAULT_LANG_POS_TO)
            withContext(Dispatchers.Main){
                binding.spinnerLanguageFrom.findSelectedItemIndexById(lngFrom)
                binding.spinnerLanguageTo.findSelectedItemIndexById(lngTo)
                mLanguageCodeFrom = languagesCodes[binding.spinnerLanguageFrom.getSelectedItemIndex()]
                mLanguageCodeTo = languagesCodes[binding.spinnerLanguageTo.getSelectedItemIndex()]
            }
        }



    }

    private fun keyboard() {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (requireActivity().currentFocus != null) {

            imm.showSoftInput(binding.textInput, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun promptSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mLanguageCodeFrom)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt))
        try {
            speechToTextResult.launch(intent)
        } catch (a: ActivityNotFoundException) {
            CustomMessage.showToast(getString(R.string.language_not_supported), requireContext())
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("NewApi")
    private fun speakOut(textMessage: String, languageCode: String) {
        val result: Int = tts?.setLanguage(Locale(languageCode)) ?: return
        if (result == TextToSpeech.LANG_MISSING_DATA) {
            CustomMessage.showToast(getString(R.string.language_pack_missing), requireContext())
            val installIntent = Intent()
            installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
            startActivity(installIntent)
        } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
            CustomMessage.showToast(getString(R.string.language_not_supported), requireContext())
        } else {

            val sdk = Build.VERSION.SDK_INT
            if (sdk >= Build.VERSION_CODES.LOLLIPOP) {
                val utteranceId = this.hashCode().toString() + ""
                tts?.speak(textMessage, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            } else {
                val map = HashMap<String, String>()
                map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "UniqueID"
                tts?.speak(textMessage, TextToSpeech.QUEUE_FLUSH, map)
            }

        }
    }

    private fun sendChatMessage(textTranslated: String, chatInput: String) {
        val from: String
        val to: String

        val isRightToLeft = resources.getBoolean(R.bool.is_right_to_left)
        if (!isRightToLeft) {
            from = mLanguageCodeFrom
            to = mLanguageCodeTo
            chatArrayAdapter!!.add(ChatMess(
                mLeft = true,
                mTranslate = false,
                mMessage = chatInput,
                mLanguageCode = from
            ))
            chatArrayAdapter!!.add(ChatMess(
                mLeft = false,
                mTranslate = true,
                mMessage = textTranslated,
                mLanguageCode = to
            ))
        }
        else {
            from = mLanguageCodeTo
            to = mLanguageCodeFrom
            chatArrayAdapter!!.add(ChatMess(
                mLeft = false,
                mTranslate = true,
                mMessage = chatInput,
                mLanguageCode = from
            ))
            chatArrayAdapter!!.add(ChatMess(
                mLeft = true,
                mTranslate = false,
                mMessage = textTranslated,
                mLanguageCode = to
            ))
        }
        chatArrayAdapter!!.notifyDataSetChanged()
    }


    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatArrayAdapter = ChatArrayAdapter(requireContext(), R.layout.chat_left)
        binding.listChatView.adapter = chatArrayAdapter
        binding.listChatView.transcriptMode = AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
        binding.listChatView.adapter = chatArrayAdapter
        //to scroll the list view to bottom on data change
        chatArrayAdapter!!.registerDataSetObserver(object : DataSetObserver() {
            override fun onChanged() {
                super.onChanged()
                binding.listChatView.setSelection(chatArrayAdapter!!.count - 1)
            }
        })
                var translationMode: String
                var langFrom: String = ""
                var langTo: String = ""
                var langFromPosition = 0
                var langToPosition = 0
                var textFrom: String
                var textTo: String

                val b: Bundle? = arguments
                b?.let { bundle ->
                    translationMode = bundle.getString(Consts.TRANSLATEMODE)?: ""
                    langFrom = bundle.getString(Consts.langFrom)?: ""
                    langTo = bundle.getString(Consts.langTo)?: ""
                    langFromPosition = bundle.getInt(Consts.langFromPosition)
                    langToPosition = bundle.getInt(Consts.langToPosition)
                    textFrom = bundle.getString(Consts.textFrom)?: ""
                    textTo = bundle.getString(Consts.textTo)?: ""


                    if (textFrom.isNotEmpty() && textTo.isNotEmpty()) {
                        val chatInput = textFrom
                        sendChatMessage(textTo, chatInput)
                    }
                    else if (textFrom.isNotEmpty() && translationMode == Consts.CONVERSATIONTRANSLATE){
                        binding.textInput.setText(textFrom)
                    }
                }
                setLanguagesSpinner(langFrom, langTo,langFromPosition, langToPosition)

        keyboard()

        binding.textInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.scan.visibility = View.VISIBLE
                binding.imgMic.visibility = View.VISIBLE
                binding.imageSend.isEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()) if (s.toString().trim { it <= ' ' }.isEmpty()) {
                    binding.textInput.setText("")
                } else {
                    binding.scan.visibility = View.GONE
                    binding.imgMic.visibility = View.GONE
                    binding.imageSend.isEnabled = true
                }
            }

        })
        conversationViewModel.resourceState.observe(viewLifecycleOwner){ resource ->
            binding.progress.visibility = View.GONE
            binding.imageSend.isEnabled = true
            when (resource){
                is Resource.Success -> {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val translated = resource.result as String
                            val translationHistory: History
                            val isFarsi = requireActivity().resources.getBoolean(R.bool.is_farsi)
                            if (isFarsi) {
                                var conf: Configuration = requireActivity().resources.configuration
                                conf = Configuration(conf)
                                conf.setLocale(Locale(Consts.LANGUAGE.en))
                                val localizedContext =
                                    requireActivity().createConfigurationContext(conf)
                                val res = localizedContext.resources
                                if (languagesNamesRes == null) {
                                    languagesNamesRes = res.getStringArray(R.array.language_names)
                                }
                                val convertedFromLang =
                                    languagesNamesRes!![binding.spinnerLanguageFrom.getSelectedItemIndex()]
                                val convertedToLang =
                                    languagesNamesRes!![binding.spinnerLanguageTo.getSelectedItemIndex()]
                                translationHistory = History(
                                    convertedFromLang,
                                    convertedToLang,
                                    binding.spinnerLanguageFrom.getSelectedItemIndex(),
                                    binding.spinnerLanguageTo.getSelectedItemIndex(),
                                    binding.textInput.text.toString().trim(),
                                    translated,
                                    System.currentTimeMillis().toString()
                                )
                            } else {
                                translationHistory = History(
                                    binding.spinnerLanguageFrom.getSelectedItemName(),
                                    binding.spinnerLanguageTo.getSelectedItemName(),
                                    binding.spinnerLanguageFrom.getSelectedItemIndex(),
                                    binding.spinnerLanguageTo.getSelectedItemIndex(),
                                    binding.textInput.text.toString().trim(),
                                    translated,
                                    System.currentTimeMillis().toString()
                                )
                            }

                        val db: AppDatabase = AppDatabase.instance(requireContext().applicationContext)

                            withContext(Dispatchers.IO) {
                                try {
                                    db.historyDao().addHistoryTranslation(translationHistory)
                                } catch (e: Exception) {
                                    if (e is CancellationException) throw e
                                    e.printStackTrace()
                                }
                            }

                            val chatInput = binding.textInput.text?.trim().toString()
                            sendChatMessage(translated, chatInput)
                            binding.textInput.setText("")

                    }
                }
                is Resource.Failed -> {
                    val exception = resource.exception
                    if (exception == null){
                        CustomMessage.showToast(getString(R.string.internet_error), requireContext())
                    }
                    else {
                        when (exception) {
                            is IOException -> {
                                exception.printStackTrace()
                                CustomMessage.showToast(
                                    getString(R.string.internet_error),
                                    requireContext()
                                )
                            }

                            is JSONException -> {
                                exception.printStackTrace()
                                CustomMessage.showToast(
                                    getString(R.string.readingerror),
                                    requireContext()
                                )
                            }

                            else -> {
                                exception.printStackTrace()
                                CustomMessage.showToast(
                                    getString(R.string.unexpectederror),
                                    requireContext()
                                )
                            }
                        }
                    }
                }
            }
        }
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        sharedViewModel.consentPermit.observe(viewLifecycleOwner){ loadAds ->

            if (loadAds){
                if (ads == null){
                    ads = UnityAdsUtil(requireContext()).apply {
                        loadInterstitial()
                    }

                }
            }

        }

        }

    override fun onPause() {
        tts?.let {
            it.stop()
            it.shutdown()
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        //Initialize tts engine as soon as activity resumes/starts/is created
        tts = TextToSpeech(requireContext().applicationContext) { i ->
            if (i != TextToSpeech.ERROR) {
                tts?.language = Locale.ENGLISH
            }
        }
    }

    override fun onDestroyView() {

        ads?.destroyInterstitial()
        chatArrayAdapter = null
        tts = null
        _binding = null
        super.onDestroyView()
    }
}