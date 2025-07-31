@file:Suppress("DEPRECATION")

package farsi.toenglish.translation.uoi.fragments


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.*
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.ClipboardManager
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import farsi.toenglish.translation.R
import farsi.toenglish.translation.data.Resource
import farsi.toenglish.translation.database.AppDatabase
import farsi.toenglish.translation.database.History
import farsi.toenglish.translation.databinding.FragmentTranslateBinding
import farsi.toenglish.translation.uoi.activities.SharedViewModel
import farsi.toenglish.translation.uoi.fragments.viemodels.TranslateViewmodel
import farsi.toenglish.translation.utils.*
import farsi.toenglish.translation.utils.Consts
import kotlinx.coroutines.CancellationException
import pl.utkala.searchablespinner.*
import java.io.IOException
import java.util.*


class TranslateFragment : Fragment() {
    private var _binding: FragmentTranslateBinding? = null
    private val binding get() = _binding!!
    private var ads: UnityAdsUtil? = null
    private var tts: TextToSpeech? = null
    private var mLanguageCodeFrom = Consts.LANGUAGE.en
    private var mLanguageCodeTo = Consts.LANGUAGE.en
    private var languagesNamesRes: Array<out String>? = null
    private lateinit var sharedViewModel: SharedViewModel
    private val translateViewmodel by viewModels<TranslateViewmodel>()




    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // PERMISSION GRANTED
            val b = Bundle()
            b.putString(Consts.TRANSLATEMODE, Consts.NORMALTRANSLATE)
            findNavController().navigate(R.id.action_translateFragment_to_imageTranslationFragment,
                b)
        }
        else {
            CustomMessage.showToast(getString(R.string.permisionexpl),requireContext())
        }
    }

    private val speechToTextResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && null != result.data) {
                /*
                                Dialog box to show list of processed Speech to text results
                                User selects matching text to display in chat
                         */
                val data: Intent = result.data!!
                val matchTextDialog = Dialog(requireContext())




                matchTextDialog.setContentView(R.layout.dialog_matches_frag)
                matchTextDialog.setTitle(getString(R.string.select_matching_text))
                val matchesText: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
                val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    matchesText
                )
                matchTextDialog.findViewById<ListView>(R.id.list).adapter = adapter
                matchTextDialog.findViewById<ListView>(R.id.list).setOnItemClickListener { _, _, position, _ ->
                    binding.textInput.setText(matchesText[position])
                    matchTextDialog.dismiss()
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
        _binding = FragmentTranslateBinding.inflate(inflater, container, false)
        binding.clipboard.setOnClickListener{
            val animation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.scale)
            binding.clipboard.startAnimation(animation)
            if (binding.textInput.text?.trim().toString().isEmpty()){
                CustomMessage.showToast(getString(R.string.copy_emptyfield), requireContext())
                return@setOnClickListener
            }

            copyToClipboard(
                binding.textInput.text.toString()
            )
        }
        binding.clipboard1.setOnClickListener{
            val animation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.scale)
            binding.clipboard1.startAnimation(animation)
            if (binding.textTranslated.text.toString().trim().isEmpty()){
                CustomMessage.showToast(getString(R.string.copy_emptyfield), requireContext())
                return@setOnClickListener
            }
            copyToClipboard(
                binding.textTranslated.text.toString()
            )
        }
        binding.share.setOnClickListener(View.OnClickListener {
            val animation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.scale)
            binding.share.startAnimation(animation)
            if (binding.textInput.text.toString().trim().isEmpty()){
                CustomMessage.showToast(getString(R.string.share_emptyfiled), requireContext())
                return@OnClickListener
            }
            shareIt(
                binding.textInput.text.toString()
            )
        })
        binding.share1.setOnClickListener {
            val animation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.scale)
            binding.share1.startAnimation(animation)
            if (binding.textTranslated.text.toString().trim().isEmpty()){
                CustomMessage.showToast(getString(R.string.share_emptyfiled), requireContext())
                return@setOnClickListener
            }
            shareIt(
                binding.textTranslated.text.toString()
            )
        }


        binding.imgMic.setOnClickListener{
            val animation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.scale)
            binding.imgMic.startAnimation(animation)
            promptSpeechInput()
        }
        binding.imageSpeak.setOnClickListener {
            val animation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.scale)
            binding.imageSpeak.startAnimation(animation)
            if (binding.textTranslated.text.trim().toString().isEmpty()){
                CustomMessage.showToast(getString(R.string.emptyfield), requireContext())
                return@setOnClickListener
            }

            speakOut("OUT")
        }
        binding.buttonTranslate.setOnClickListener {
            val animation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.touch_release)
            binding.buttonTranslate.startAnimation(animation)
            val input: String = binding.textInput.text?.trim().toString()
            if (requireActivity().currentFocus != null) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.textInput.windowToken, 0)
            }
            binding.progress.visibility = View.VISIBLE
            binding.buttonTranslate.isEnabled = false
            translateViewmodel.translate(
                input,
                mLanguageCodeFrom,
                mLanguageCodeTo
            )


        }
        binding.imageSwap.setOnClickListener {
            val temp = mLanguageCodeFrom
            mLanguageCodeFrom = mLanguageCodeTo
            mLanguageCodeTo = temp
            val posFrom: Int = binding.spinnerLanguageFrom.getSelectedItemIndex()
            val posTo: Int = binding.spinnerLanguageTo.getSelectedItemIndex()
            binding.spinnerLanguageFrom.findSelectedItemIndexById(posTo)
            binding.spinnerLanguageTo.findSelectedItemIndexById(posFrom)
            val textFrom: String = binding.textInput.text.toString()
            val textTo: String = binding.textTranslated.text.toString()
            binding.textInput.setText(textTo)
            binding.textTranslated.text = textFrom
        }
        binding.speakAudio.setOnClickListener {
            val animation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.scale)
            binding.speakAudio.startAnimation(animation)
            if (binding.textInput.text.toString().trim().isEmpty()){
                CustomMessage.showToast(getString(R.string.emptyfield), requireContext())
                return@setOnClickListener
            }
            speakOut("IN")
        }
        binding.scan.setOnClickListener {
            val animation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.scale)
            binding.scan.startAnimation(animation)
            startCameraPermissionRequest()
        }
        binding.clearText.setOnClickListener {
            binding.textInput.setText("")
        }
        binding.homeBut.setOnClickListener {
            displayInterstitial(ads){
                findNavController().navigateUp()
            }
        }
        return binding.root

    }
    private fun startCameraPermissionRequest() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
    private fun promptSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mLanguageCodeFrom)
        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            getString(R.string.speech_prompt)
        )
        try {
            speechToTextResult.launch(intent)
        } catch (a: ActivityNotFoundException) {
            CustomMessage.showToast(getString(R.string.language_not_supported), requireContext())
        }
    }

    @Suppress("DEPRECATION")
    private fun copyToClipboard(copyText: String?) {
        if (copyText == null || copyText.trim { it <= ' ' }.isEmpty()) return
        val sdk: Int = Build.VERSION.SDK_INT
        if (sdk < Build.VERSION_CODES.HONEYCOMB) {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.text = copyText
        } else {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip: ClipData = ClipData
                .newPlainText(getString(R.string.yourtext), copyText)
            clipboard.setPrimaryClip(clip)
        }
        CustomMessage.showToast(getString(R.string.textcopied), requireContext())
    }



    @SuppressLint("NewApi")
    @Suppress("DEPRECATION")
    private fun speakOut(arg: String) {
        val textMessage:String
        val result: Int? = if (arg == "IN") {
            textMessage = binding.textInput.text.toString().trim()
            tts?.setLanguage(Locale(mLanguageCodeFrom))
        } else {
            textMessage = binding.textTranslated.text.toString().trim()
            tts?.setLanguage(Locale(mLanguageCodeTo))
        }
        if (result == null){
            CustomMessage.showToast(getString(R.string.language_not_identified), requireContext())
            return
        }
        if (result == TextToSpeech.LANG_MISSING_DATA) {
            CustomMessage.showToast(getString(R.string.language_pack_missing), requireContext())
            val installIntent = Intent()
            installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
            startActivity(installIntent)
        } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
            CustomMessage.showToast(getString(R.string.language_not_supported), requireContext())
        }
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


    private fun setLanguagesSpinner(langFrom: String, langTo: String, langFromPosition: Int, langToPosition: Int) {

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
        //binding.spinnerLanguageFrom.setCustomDialogAdapter(StringHintArrayAdapter(requireContext(), R.layout.item_spineer_list, languagesNames, null))
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
            else{
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
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (requireActivity().currentFocus != null) {
            imm.showSoftInput(binding.textInput, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun shareIt(text: String) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            resources.getString(R.string.app_name)
        )
        sharingIntent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)))
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

                var langFrom = ""
                var langTo = ""
                var langFromPosition = 0
                var langToPosition = 0
                var textFrom: String
                var textTo: String
                var translationMode: String



                val b: Bundle? = arguments
                b?.let { bundle ->
                    langFrom = bundle.getString(Consts.langFrom)?: ""
                    langTo = bundle.getString(Consts.langTo)?: ""
                    langFromPosition = bundle.getInt(Consts.langFromPosition)
                    langToPosition = bundle.getInt(Consts.langToPosition)
                    textFrom = bundle.getString(Consts.textFrom)?: ""
                    textTo = bundle.getString(Consts.textTo)?: ""
                    translationMode = bundle.getString(Consts.TRANSLATEMODE)?: ""

                    if (textFrom.isNotEmpty() && textTo.isNotEmpty()) {
                        binding.textInput.setText(textFrom)
                        binding.textTranslated.text = textTo
                    }
                    else if (textFrom.isNotEmpty() && translationMode == Consts.NORMALTRANSLATE){
                        binding.textInput.setText(textFrom)
                    }
                }


            setLanguagesSpinner(langFrom, langTo, langFromPosition, langToPosition)

            keyboard()

            binding.textTranslated.movementMethod = ScrollingMovementMethod()



        binding.textInput.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (count < 1 && s.isEmpty()) {
                    binding.speakAudio.visibility = View.GONE
                    binding.share.visibility = View.GONE
                }
                binding.scan.visibility = View.VISIBLE
                binding.imgMic.visibility = View.VISIBLE
                binding.clearText.visibility = View.GONE
                binding.buttonTranslate.isEnabled = false
            }

            override fun afterTextChanged(s: Editable) {

                if (s.toString().isNotEmpty()) if (s.toString().trim { it <= ' ' }.isEmpty()) {
                    binding.textInput.setText("")
                } else {
                    binding.speakAudio.visibility = View.VISIBLE
                    binding.share.visibility = View.VISIBLE
                    binding.scan.visibility = View.GONE
                    binding.imgMic.visibility = View.GONE
                    binding.clearText.visibility = View.VISIBLE
                    binding.buttonTranslate.isEnabled = true
                }
            }
        })
        translateViewmodel.resourceState.observe(viewLifecycleOwner){ resource ->
            binding.progress.visibility = View.GONE
            binding.buttonTranslate.isEnabled = true
            when(resource){
                is Resource.Success -> {
                    viewLifecycleOwner.lifecycleScope.launch{
                        val translated = resource.result as String
                        val translationHistory: History
                        val isFarsi = requireActivity().resources.getBoolean(R.bool.is_farsi)
                        if (isFarsi){
                            var conf: Configuration = requireActivity().resources.configuration
                            conf = Configuration(conf)
                            conf.setLocale(Locale(Consts.LANGUAGE.en))
                            val localizedContext = requireActivity().createConfigurationContext(conf)
                            val res = localizedContext.resources
                            if (languagesNamesRes == null){
                                languagesNamesRes = res.getStringArray(R.array.language_names)
                            }
                            val convertedFromLang = languagesNamesRes!![binding.spinnerLanguageFrom.getSelectedItemIndex()]
                            val convertedToLang = languagesNamesRes!![binding.spinnerLanguageTo.getSelectedItemIndex()]
                            translationHistory = History(
                                convertedFromLang,
                                convertedToLang,
                                binding.spinnerLanguageFrom.getSelectedItemIndex(),
                                binding.spinnerLanguageTo.getSelectedItemIndex(),
                                binding.textInput.text.toString().trim(),
                                translated,
                                System.currentTimeMillis().toString()
                            )
                        }
                        else {
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
                            }
                            catch (e: Exception){
                                if (e is CancellationException) throw e
                                e.printStackTrace()
                            }
                        }

                        binding.textTranslated.text = translated
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

        tts = TextToSpeech(requireContext().applicationContext) { i ->
            if (i != TextToSpeech.ERROR) {
                tts?.language = Locale.ENGLISH


            }
        }
        super.onResume()
    }



    override fun onDestroyView() {
        ads?.destroyInterstitial()
        tts = null
        _binding = null
        super.onDestroyView()
    }

}