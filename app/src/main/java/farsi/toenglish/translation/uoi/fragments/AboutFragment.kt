package farsi.toenglish.translation.uoi.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

import androidx.navigation.fragment.findNavController
import farsi.toenglish.translation.R

import farsi.toenglish.translation.databinding.FragmentAboutBinding
import farsi.toenglish.translation.uoi.activities.SharedViewModel
import farsi.toenglish.translation.utils.CustomMessage


import java.lang.Exception

class AboutFragment : Fragment() {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    private lateinit var packageManager: PackageManager
    private lateinit var sharedViewModel: SharedViewModel
    private fun openWebURL(inURL: String) {
        try {
            val browse = Intent(Intent.ACTION_VIEW, Uri.parse(inURL))
            startActivity(browse)
        } catch (e: Exception) {
            CustomMessage.showToast(getString(R.string.couldnt_open), requireContext())
        }
    }

    private val versionName: String
        get() {
            val packageName = requireActivity().packageName
            val versionName: String = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                     packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                        .versionName
                } else {
                    @Suppress("DEPRECATION")
                     packageManager.getPackageInfo(packageName, 0).versionName
                }

            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                getString(R.string.unknown)
            }
            return versionName
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       _binding = FragmentAboutBinding.inflate(layoutInflater, container, false)

        binding.privacy.setOnClickListener { openWebURL(getString(R.string.privacy_policy_url)) }
        binding.homeBut.setOnClickListener { findNavController().navigateUp() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            binding.logoMain.setImageResource(R.drawable.logo)
        } catch (ex: Exception){
            ex.printStackTrace()
        }
        packageManager = requireContext().packageManager
        val version = "${getString(R.string.version)}\n$versionName"
        binding.version.text = version
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        sharedViewModel.consentPermit.observe(viewLifecycleOwner){ loadAds ->
            if (loadAds){
                sharedViewModel.disableAds()
            }
        }

    }

    override fun onDestroyView() {

        _binding = null
        super.onDestroyView()
    }

}