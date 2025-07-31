package farsi.toenglish.translation.uoi.fragments.viemodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import farsi.toenglish.translation.data.Resource
import farsi.toenglish.translation.network.Api
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.lang.Exception

class TranslateViewmodel: ViewModel() {
    private var _resourceState = MutableLiveData<Resource<Any>>()
    val resourceState = _resourceState as LiveData<Resource<Any>>
    fun translate(
        input: String, mLanguageCodeFrom: String,
        mLanguageCodeTo: String
    ) {
        viewModelScope.launch {
            val translated: String?
            try {
                translated = Api.translateQuickGoogle(
                    input,
                    mLanguageCodeFrom,
                    mLanguageCodeTo
                )
                if (translated == null){
                    _resourceState.value = Resource.Failed(null)
                }
                else {
                    _resourceState.value = Resource.Success(translated)
                }

            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _resourceState.value = Resource.Failed(e)
            }
        }
    }
}