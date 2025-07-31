package farsi.toenglish.translation.uoi.fragments



import android.R
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import farsi.toenglish.translation.adapters.BookmarkAdapter


import farsi.toenglish.translation.databinding.FragmentBookmarkBinding
import farsi.toenglish.translation.uoi.activities.SharedViewModel
import farsi.toenglish.translation.utils.UnityAdsUtil


class BookmarkFragment : Fragment() {
    private var _binding: FragmentBookmarkBinding? = null
    private val binding get() = _binding!!
    private var ads: UnityAdsUtil? = null
    private var bookmarkAdapter: farsi.toenglish.translation.adapters.BookmarkAdapter? = null
    private val viewModelBookmark by viewModels<ViewModelBookmark>()
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


            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                callback)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        _binding = FragmentBookmarkBinding.inflate(layoutInflater, container, false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



           viewModelBookmark.getBookmark().observe(viewLifecycleOwner) { translationBookmark ->
               bookmarkAdapter = BookmarkAdapter(
                   requireActivity(),
                   R.layout.activity_list_item,
                   translationBookmark
               )
               binding.lvBookmarks.adapter = bookmarkAdapter
               binding.lvBookmarks.emptyView = binding.emptyBookmark
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
        binding.homeBut.setOnClickListener {
            displayInterstitial(ads){
                findNavController().navigateUp()
            }
        }
    }
    override fun onDestroyView() {
        ads?.destroyInterstitial()
        bookmarkAdapter?.clear()
        bookmarkAdapter = null
        _binding = null
        super.onDestroyView()
    }

}