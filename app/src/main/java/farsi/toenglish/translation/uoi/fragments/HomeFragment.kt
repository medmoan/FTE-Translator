package farsi.toenglish.translation.uoi.fragments



import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import farsi.toenglish.translation.R
import farsi.toenglish.translation.databinding.FragmentHomeBinding

import farsi.toenglish.translation.uoi.activities.SharedViewModel
import farsi.toenglish.translation.utils.Consts
import farsi.toenglish.translation.utils.CustomMessage



class HomeFragment : Fragment(), View.OnClickListener{
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedViewModel: SharedViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        binding.startNewTranslation.setOnClickListener(this@HomeFragment)
        binding.startNewConversation.setOnClickListener(this@HomeFragment)
        binding.dic.setOnClickListener(this@HomeFragment)
        binding.setting.setOnClickListener(this@HomeFragment)
        binding.historyActivity.setOnClickListener(this@HomeFragment)
        binding.bookmarkActivity.setOnClickListener(this@HomeFragment)
        binding.overflowBut.setOnClickListener(this@HomeFragment)
        binding.rateUs.setOnClickListener(this@HomeFragment)
        return binding.root
    }

    private fun shareApp() {
         Intent(Intent.ACTION_SEND).apply {
            this.type = "text/plain"
            this.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name))
            this.putExtra(
                Intent.EXTRA_TEXT, """${resources.getString(R.string.quick_share)} 
                    ${
                    resources.getString(
                        R.string.app_name
                    )
                }
                ${Consts.WEB_CAFEBAZAAR_URL}"""
            )
        }.also {
             try {
                 requireActivity().startActivity(Intent.createChooser(it, "Share"))
             }
             catch (e: Exception){
                 e.printStackTrace()
                 CustomMessage.showToast(getString(R.string.couldnt_share), requireContext())
             }

        }


    }
    private fun rateUs() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Consts.MARKET_URL + requireActivity().packageName))
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            requireActivity().startActivity(intent)
        } catch (anfe: ActivityNotFoundException) {
            anfe.printStackTrace()
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Consts.WEB_CAFEBAZAAR_URL + requireActivity().packageName))
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                requireActivity().startActivity(intent)
            }
            catch (anfe: ActivityNotFoundException){
                anfe.printStackTrace()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            binding.logoMain.setImageResource(R.drawable.logo)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }



        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        sharedViewModel.consentPermit.observe(viewLifecycleOwner){ loadAds ->

            if (loadAds){
                sharedViewModel.allowShowAds()
            }

        }

    }
    override fun onClick(v: View?) {

        when (v?.id){
            R.id.start_new_translation -> navigate(R.id.action_homeFragment_to_translateFragment)


            R.id.start_new_conversation -> navigate(R.id.action_homeFragment_to_conversationFragment)
            R.id.dic -> navigate(R.id.action_homeFragment_to_dictionaryFragment)
            R.id.setting -> navigate(R.id.action_homeFragment_to_settingFragment)
            R.id.historyActivity -> navigate(R.id.action_homeFragment_to_historyFragment)
            R.id.bookmarkActivity -> navigate(R.id.action_homeFragment_to_bookmarkFragment)
            R.id.overflow_but -> {

                val popupMenu = PopupMenu(
                    requireContext(), (v)
                )
                popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.about -> {
                            navigate(R.id.action_homeFragment_to_aboutFragment)
                            return@OnMenuItemClickListener true
                        }
                        R.id.shareapp -> {
                            shareApp()
                            return@OnMenuItemClickListener true
                        }
                        else -> {
                            return@OnMenuItemClickListener false
                        }
                    }
                })
                popupMenu.inflate(R.menu.popup_menu_home)

                sharedViewModel.isGDPR.observe(viewLifecycleOwner){ isGDPR ->
                    if (isGDPR){
                        val privacySettings = popupMenu.menu.findItem(R.id.privacysettings)
                        privacySettings.isVisible = true
                    }
                }


                popupMenu.show()
            }
            R.id.rateUs -> rateUs()
        }


}

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }



}
fun Fragment.navigate(directions: Int) {
    val controller = findNavController()
    val currentDestination = (controller.currentDestination as? FragmentNavigator.Destination)?.className
        ?: (controller.currentDestination as? DialogFragmentNavigator.Destination)?.className
    if (currentDestination == this.javaClass.name) {
        controller.navigate(directions)
    }
}