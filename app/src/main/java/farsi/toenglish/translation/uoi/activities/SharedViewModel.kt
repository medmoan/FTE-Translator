package farsi.toenglish.translation.uoi.activities


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class SharedViewModel : ViewModel() {

    var consentPermit = MutableLiveData<Boolean>()
    var isGDPR = MutableLiveData<Boolean>()
    var allowedAds = MutableLiveData<Boolean>()
        private set
    var statusSnackbar = MutableLiveData<Boolean>()
        private set
    fun allowShowAds(){
        allowedAds.value = true
    }
    fun disableAds(){
        allowedAds.value = false
    }
    fun openedSnackbar(){
        statusSnackbar.value = true
    }
    fun dismissedSnackbar(){
        statusSnackbar.value = false
    }
}