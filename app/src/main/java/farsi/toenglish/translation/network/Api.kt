package farsi.toenglish.translation.network



import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

import org.json.JSONArray
import org.json.JSONObject

import java.net.URLEncoder


object Api {

    @Throws(Exception::class)
    suspend fun translateQuickGoogle(
        inputText: String,
        fromLangCode: String,
        toLangCode: String
    ): String? {
        val res = withContext(Dispatchers.IO) {
            var inputTxtChanged = URLEncoder.encode(inputText, "utf-8")
            val fromLangCodeChanged = "&sl=$fromLangCode"
            val toLangCodeChanged = "&tl=$toLangCode"
            inputTxtChanged = "&q=$inputTxtChanged"
            val translatedText: StringBuilder = java.lang.StringBuilder()
            val userAgent = "User-Agent"
            val userAgentContent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36"
            val urlTranslation =
                "https://translate.googleapis.com/translate_a/single?client=gtx&dt=t&dt=bd&dj=1&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=at$toLangCodeChanged$fromLangCodeChanged$inputTxtChanged"
            val client = OkHttpClient()
            val request: Request = Request.Builder()
                .header(
                    userAgent,
                    userAgentContent
                )
                .url(urlTranslation)
                .build()

            val respond = client.newCall(request).execute()
            if (!respond.isSuccessful) return@withContext null
            val result = respond.body?.string() ?: return@withContext null
            result.ifEmpty {
                return@withContext null
            }
            val jSONArray: JSONArray = JSONObject(result).getJSONArray("sentences")
            val i = jSONArray.length()
            if (i <= 0) {
                return@withContext null
            }
            for (index in 0 until i) {
                ensureActive()
                val jSONObject = jSONArray.getJSONObject(index)
                val trans = try {
                    jSONObject.getString("trans")
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@withContext translatedText.toString().ifEmpty {
                        null
                    }
                }
                trans.let { text ->
                    if (text.isNotEmpty()) translatedText.append(text)
                }

            }
            return@withContext translatedText.toString().ifEmpty {
                    null
            }

        }
        return res
    }
}
