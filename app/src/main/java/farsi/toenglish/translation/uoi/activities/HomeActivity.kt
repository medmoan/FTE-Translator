package farsi.toenglish.translation.uoi.activities



import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import farsi.toenglish.translation.R
import farsi.toenglish.translation.utils.AppRater
import farsi.toenglish.translation.utils.Preferences
import farsi.toenglish.translation.utils.UnityAdsUtil


class HomeActivity : AppCompatActivity() {


    private var ads: UnityAdsUtil? = null
    private val sharedViewModel by viewModels<SharedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().apply {
//            UnityAdsUtil.init(this@HomeActivity)
//            StartioAds.testAds(false)
        }
        setContentView(R.layout.activity_home)
        val preference = Preferences.instance(this)
        if (savedInstanceState == null){
            AppRater.appLaunched(this, lifecycleScope, preference)
        }
        sharedViewModel.consentPermit.value = true
        sharedViewModel.statusSnackbar.observe(this){

        }
        sharedViewModel.consentPermit.observe(this){ loadAds ->
            if (loadAds){
                ads = UnityAdsUtil(this).apply {
                    loadBanner(
                        container = findViewById(R.id.ads)
                    )
                }
//                sharedViewModel.allowedAds.observe(this){ allowedShowAds ->
//                    if (allowedShowAds){
//                        ads!!.lo()
//                    }
//                    else{
//                        ads!!.hideBanner()
//                    }
//                }
            }
        }


    }


    override fun onDestroy() {
        ads?.destroyBanner()
        ads = null
        super.onDestroy()
    }



}