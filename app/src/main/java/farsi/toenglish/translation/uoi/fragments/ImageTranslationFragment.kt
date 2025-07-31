package farsi.toenglish.translation.uoi.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import farsi.toenglish.translation.R
import farsi.toenglish.translation.databinding.FragmentImageTranslationBinding
import farsi.toenglish.translation.uoi.activities.SharedViewModel
import farsi.toenglish.translation.utils.Consts
import farsi.toenglish.translation.utils.CustomMessage


class ImageTranslationFragment: Fragment(){
    private var _binding: FragmentImageTranslationBinding? = null
    private val binding get() = _binding!!
    private lateinit var handler: Handler
    private var cameraSource: CameraSource? = null
    private var stringText: String? = null
    private var textRecognizer: TextRecognizer? = null
    @Volatile private var stop = false
    private lateinit var sharedViewModel: SharedViewModel

    private fun textRecognizer() {
        textRecognizer = TextRecognizer.Builder(requireContext()).build()
        cameraSource = CameraSource.Builder(requireContext(), textRecognizer as Detector<*>).setRequestedPreviewSize(300, 300).setAutoFocusEnabled(true).build()
        binding.surfaceV.holder.addCallback(object: SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {}

            @SuppressLint("MissingPermission")
            override fun surfaceCreated(param1SurfaceHolder: SurfaceHolder ) {
                try {
                    cameraSource?.start(binding.surfaceV.holder)
                }
                catch (e: Exception){
                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(param1SurfaceHolder: SurfaceHolder) {
                cameraSource?.stop()
            }


        })
        textRecognizer?.setProcessor(object: Detector.Processor<TextBlock> {


            override fun release() {}

            override fun receiveDetections(param1Detections: Detector.Detections<TextBlock>) {
                val sparseArray: SparseArray<TextBlock> = param1Detections.detectedItems
                val stringBuilder = StringBuilder()

                for (i in 0 until sparseArray.size()){
                    val textBlock = sparseArray.valueAt(i)
                    if (textBlock?.value != null) {
                        val stringBuilder1 = StringBuilder()
                        stringBuilder1.append(textBlock.value);
                        stringBuilder1.append(" ");
                        stringBuilder.append(stringBuilder1.toString());
                    }
                }
                stringText = stringBuilder.toString()
                handler = Handler(Looper.getMainLooper())
                handler.post(object: Runnable {


                     override fun run() {
                         if (stop) return
                         binding.textV.text = stringText
                     }
                 })
            }
        });
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        _binding = FragmentImageTranslationBinding.inflate(inflater, container, false)
        binding.homeBut.setOnClickListener {
            findNavController().navigateUp()
        }
        textRecognizer()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabCamera.setOnClickListener {
            if (stringText == null) {
                CustomMessage.showToast(
                    getString(R.string.not_get_text),
                    requireContext()
                )
            } else {
                val text = binding.textV.text.trim().toString()
                if (text.isEmpty()) {
                    return@setOnClickListener
                }
                val arg = arguments
                val translateMode = arg?.getString(Consts.TRANSLATEMODE) ?: ""
                val b = Bundle()
                b.putString(Consts.TRANSLATEMODE, translateMode)
                b.putString(Consts.textFrom, text)
                if (translateMode == Consts.NORMALTRANSLATE) {
                    findNavController().navigate(
                        R.id.action_imageTranslationFragment_to_translateFragment,
                        b
                    )
                } else if (translateMode == Consts.CONVERSATIONTRANSLATE) {
                    findNavController().navigate(
                        R.id.action_imageTranslationFragment_to_conversationFragment,
                        b
                    )
                }
            }
        }
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        sharedViewModel.disableAds()
    }

    override fun onDestroyView() {
        stop = true
        cameraSource?.release()
        _binding = null
        super.onDestroyView()
    }
}