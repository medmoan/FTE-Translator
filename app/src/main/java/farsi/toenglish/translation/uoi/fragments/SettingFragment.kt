package farsi.toenglish.translation.uoi.fragments

import android.app.LocaleManager
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Spinner

import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import farsi.toenglish.translation.R
import farsi.toenglish.translation.databinding.FragmentSettingBinding
import farsi.toenglish.translation.uoi.activities.SharedViewModel
import farsi.toenglish.translation.utils.*
import farsi.toenglish.translation.utils.Consts

import pl.utkala.searchablespinner.OnSearchableItemClick
import pl.utkala.searchablespinner.StringHintArrayAdapter
import java.util.ArrayList

class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private var ads: UnityAdsUtil? = null
    private val binding get() = _binding!!


    private lateinit var sharedViewModel: SharedViewModel



    private fun setLanguagesSpinner() {
        val languagesNamesRes = requireActivity().resources.getStringArray(R.array.language_names)
        val languagesNames: List<String> = ArrayList(listOf(*languagesNamesRes))


        binding.spinnerLanguageFrom.adapter = StringHintArrayAdapter(requireContext(), R.layout.simple_list_item, languagesNames)
        binding.spinnerLanguageFrom.start()
        binding.spinnerLanguageFrom.onSearchableItemClick = object : OnSearchableItemClick<Any?> {
            override fun onSearchableItemClicked(item: Any?, position: Int) {
                if (position > 0) {
                    binding.spinnerLanguageFrom.setLastSelectedItemIndex(position)

                } else {
                    binding.spinnerLanguageFrom.setSelection(Spinner.INVALID_POSITION)

                }
            }
        }
        binding.spinnerLanguageTo.adapter = StringHintArrayAdapter(requireContext(), R.layout.simple_list_item, languagesNames)
        binding.spinnerLanguageTo.start()
        binding.spinnerLanguageTo.onSearchableItemClick = object : OnSearchableItemClick<Any?> {
            override fun onSearchableItemClicked(item: Any?, position: Int) {
                if (position > 0) {
                    binding.spinnerLanguageTo.setLastSelectedItemIndex(position)

                } else {
                    binding.spinnerLanguageTo.setSelection(Spinner.INVALID_POSITION)

                }
            }
        }
        val appPref = Preferences.instance(requireContext().applicationContext)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
            val lngFrom: Int = appPref.getInt(Consts.LANGFROM, Consts.DEFAULT_LANG_POS_FROM)
            val lngTo: Int = appPref.getInt(Consts.LANGTO, Consts.DEFAULT_LANG_POS_TO)
        withContext(Dispatchers.Main){
            binding.spinnerLanguageFrom.findSelectedItemIndexById(lngFrom)
            binding.spinnerLanguageTo.findSelectedItemIndexById(lngTo)
        }
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
        _binding = FragmentSettingBinding.inflate(layoutInflater, container, false)

        binding.apply.setOnClickListener {
            val animation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.touch_release)
            binding.apply.startAnimation(animation)
            val appPref = Preferences.instance(requireContext().applicationContext)
            val lngFromSelectedItem = binding.spinnerLanguageFrom.getSelectedItemIndex()
            val lngToSelectedItem = binding.spinnerLanguageTo.getSelectedItemIndex()
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
                val lngFrom: Int = appPref.getInt(Consts.LANGFROM, Consts.DEFAULT_LANG_POS_FROM)
                val lngTo: Int = appPref.getInt(Consts.LANGTO, Consts.DEFAULT_LANG_POS_TO)
                if (lngFromSelectedItem == Consts.DEFAULT_LANG_POS_FROM
                    && lngToSelectedItem == Consts.DEFAULT_LANG_POS_TO
                ) {
                   return@launch
                } else if (lngFromSelectedItem == lngFrom
                    && lngToSelectedItem == lngTo
                ) {
                    return@launch
                } else {
                    appPref.save(Consts.LANGFROM, lngFromSelectedItem)
                    appPref.save(Consts.LANGTO, lngToSelectedItem)

                }
            }

            CustomMessage.showToast(getString(R.string.savedchanges), requireContext())
            val englishChecked = binding.englishLang.isChecked
            val persianChecked = binding.persianLang.isChecked

            if (!englishChecked && !persianChecked){
                return@setOnClickListener
            }
            val isFarsi = requireActivity().resources.getBoolean(R.bool.is_farsi)
            if (englishChecked && !isFarsi ||
                    persianChecked && isFarsi){
                return@setOnClickListener
            }

            englishChecked.let { bool ->
                if (bool){
                    setLocale(Consts.LANGUAGE.en)
                }
            }
            persianChecked.let { bool ->
                if (bool){
                    setLocale(Consts.LANGUAGE.fa)
                }
            }
//            requireActivity().recreate()
            val intent = requireActivity().intent
            requireActivity().finish()
            requireActivity().startActivity(intent)

        }
        binding.homeBut.setOnClickListener {
            displayInterstitial(ads){
                findNavController().navigateUp()
            }
        }
        return binding.root
    }

    private fun setLocale(langCode: String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requireContext().getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags(langCode)
        }else{
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langCode))
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setLanguagesSpinner()

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

    override fun onDestroyView() {
        ads?.destroyInterstitial()
        _binding = null
        super.onDestroyView()
    }

}