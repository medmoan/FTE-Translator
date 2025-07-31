package farsi.toenglish.translation.modules

class ChatMess(
    private val mLeft: Boolean,
    private val mTranslate: Boolean,
    private val mMessage: String,
    private val mLanguageCode: String
) {
    fun getmMessage(): String {
        return mMessage
    }

    fun getmLeft(): Boolean {
        return mLeft
    }

    fun getmLanguageCode(): String {
        return mLanguageCode
    }

    fun getmTranslate(): Boolean {
        return mTranslate
    }
}