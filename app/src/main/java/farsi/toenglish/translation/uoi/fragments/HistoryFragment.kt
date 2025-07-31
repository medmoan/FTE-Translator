package farsi.toenglish.translation.uoi.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import farsi.toenglish.translation.adapters.HistoryAdapter

import farsi.toenglish.translation.databinding.FragmentHistoryBinding
import farsi.toenglish.translation.uoi.activities.SharedViewModel
import farsi.toenglish.translation.utils.UnityAdsUtil

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private var ads: UnityAdsUtil? = null
    private val binding get() = _binding!!
    private var historyAdapter: HistoryAdapter? = null
    private val viewModelHistory by viewModels<ViewModelHistory>()
    private lateinit var sharedViewModel: SharedViewModel

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
        _binding = FragmentHistoryBinding.inflate(layoutInflater, container,false)
        binding.homeBut.setOnClickListener {
            displayInterstitial(ads){
                findNavController().navigateUp()
            }
        }
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




            viewModelHistory.getHistory().observe(viewLifecycleOwner
            ) { translationHistory ->
                historyAdapter = HistoryAdapter(
                    requireContext(),
                    android.R.layout.activity_list_item,
                    translationHistory
                )
                binding.lvHistory.adapter = historyAdapter
                binding.lvHistory.emptyView = binding.emptyHistory
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

    override fun onDestroyView() {
        ads?.destroyInterstitial()
        historyAdapter?.clear()
        historyAdapter = null
        _binding = null
        super.onDestroyView()
    }

}