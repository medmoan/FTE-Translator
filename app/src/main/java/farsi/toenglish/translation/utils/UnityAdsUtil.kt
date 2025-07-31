package farsi.toenglish.translation.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAds.UnityAdsLoadError
import com.unity3d.ads.UnityAds.UnityAdsShowCompletionState
import com.unity3d.ads.UnityAds.UnityAdsShowError
import com.unity3d.ads.UnityAdsShowOptions
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.BannerView.IListener
import com.unity3d.services.banners.UnityBannerSize
import java.lang.Exception

// requires activity context
// needs to be initialized after the ui is loaded, after (setContentView)
class UnityAdsUtil(private val context: Context): IUnityAdsInitializationListener {
     private lateinit var container: LinearLayoutCompat
     private var loadedInterstial = false
     private var showListener: IUnityAdsShowListener? = object : IUnityAdsShowListener {
        override fun onUnityAdsShowFailure(
            placementId: String,
            error: UnityAdsShowError,
            message: String
        ) {
            Log.e(
                TAG,
                "Unity Ads failed to show ad for $placementId with error: [$error] $message"
            )
            loadedInterstial = false
        }

        override fun onUnityAdsShowStart(placementId: String) {
            Log.v(TAG, "onUnityAdsShowStart: $placementId")
        }

        override fun onUnityAdsShowClick(placementId: String) {
            Log.v(TAG, "onUnityAdsShowClick: $placementId")
        }

        override fun onUnityAdsShowComplete(
            placementId: String,
            state: UnityAdsShowCompletionState
        ) {
            Log.v(TAG, "onUnityAdsShowComplete: $placementId")

        }
    }
    private var loadListener: IUnityAdsLoadListener? = object : IUnityAdsLoadListener {
        override fun onUnityAdsAdLoaded(placementId: String) {
            loadedInterstial = true
        }

        override fun onUnityAdsFailedToLoad(
            placementId: String,
            error: UnityAdsLoadError,
            message: String
        ) {
            Log.e(
                TAG,
                "Unity Ads failed to load ad for $placementId with error: [$error] $message"
            )
            loadedInterstial = false
        }
    }
    private val bannerListener: IListener = object : IListener {
        override fun onBannerLoaded(bannerAdView: BannerView) {
            // Called when the banner is loaded.
            Log.v(TAG, "onBannerLoaded: " + bannerAdView.placementId)
            container.visibility = View.VISIBLE

        }

        override fun onBannerFailedToLoad(bannerAdView: BannerView, errorInfo: BannerErrorInfo) {
            Log.e(
                TAG,
                "Unity Ads failed to load banner for " + bannerAdView.placementId + " with error: [" + errorInfo.errorCode + "] " + errorInfo.errorMessage
            )
            container.visibility = View.GONE

            // Note that the BannerErrorInfo object can indicate a no fill (refer to the API documentation).
        }

        override fun onBannerClick(bannerAdView: BannerView) {
            // Called when a banner is clicked.
            Log.v(TAG, "onBannerClick: " + bannerAdView.placementId)
        }

        override fun onBannerLeftApplication(bannerAdView: BannerView) {
            // Called when the banner links out of the application.
            Log.v(TAG, "onBannerLeftApplication: " + bannerAdView.placementId)
        }
    }
    private var bannerView: BannerView? = null
    init {
        UnityAds.initialize(context, unityGameID, TESTMODE, this)
    }
    override fun onInitializationComplete() {

    }

    override fun onInitializationFailed(
        error: UnityAds.UnityAdsInitializationError?,
        message: String?
    ) {
        Log.e(TAG, "Unity Ads initialization failed with error: [" + error + "] " + message)
    }
    fun loadInterstitial() {
        UnityAds.load(INTERSTITIALADUNITID, loadListener)
    }
    fun loadBanner(container: LinearLayoutCompat){
        try {
            val activity = context as Activity
            this.container = container
            bannerView = BannerView(activity, BANNERADUNITID, UnityBannerSize(320, 50))
            bannerView!!.listener = bannerListener
            bannerView!!.load()
            this.container.addView(bannerView)
        }
        catch (e: Exception){
            e.printStackTrace()
        }

    }
    fun showInterstitial(onNavigate: () -> Unit){
        try {
            if (loadedInterstial && mCountClick >= FrequenceShowAd){
                val activity = context as Activity
                mCountClick = 0
                UnityAds.show(activity, INTERSTITIALADUNITID, UnityAdsShowOptions(), showListener)
            }
            else{
                ++ mCountClick
                onNavigate()
            }
        }
        catch (e: Exception){
            e.printStackTrace()
            onNavigate()
        }

    }

    


    fun destroyBanner(){
        bannerView?.removeAllViews()
        bannerView = null
    }
    fun destroyInterstitial(){
        showListener = null
        loadListener = null
    }
    companion object {
        private const val unityGameID = "5448103"
        private const val TESTMODE = true
        private const val INTERSTITIALADUNITID = "Interstitial_Android"
        private const val BANNERADUNITID = "Banner_Android"
        private const val TAG = "UnityAdsUtil"
        private const val FrequenceShowAd = 3
        private var mCountClick = 0
    }
}