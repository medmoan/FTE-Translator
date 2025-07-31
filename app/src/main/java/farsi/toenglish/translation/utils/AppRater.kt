package farsi.toenglish.translation.utils

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import farsi.toenglish.translation.R


object AppRater {

    private const val DAYS_UNTIL_PROMPT = 0 //Min number of days
    private const val LAUNCHES_UNTIL_PROMPT = 5 //Min number of launches

    fun appLaunched(mContext: Context, scope: CoroutineScope, preferences: Preferences) {

        scope.launch {
            if (preferences.getBoolean("dontshowagain", false)) return@launch
            val launchCount: Int = preferences.getInt("launch_count", 0) + 1
            preferences.save("launch_count", launchCount)

            var dateFirstlaunch: Long = preferences.getLong("date_firstlaunch", 0L)
            if (dateFirstlaunch == 0L) {
                dateFirstlaunch = System.currentTimeMillis()
                preferences.save("date_firstlaunch", dateFirstlaunch)
            }
            if (launchCount >= LAUNCHES_UNTIL_PROMPT) {
                if (System.currentTimeMillis() >= dateFirstlaunch +
                    DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000
                ) {
                        withContext(Dispatchers.Main){
                            showRateDialog(mContext, scope, preferences)
                        }

                }
            }
        }
    }

    private fun rateUs(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Consts.MARKET_URL + context.packageName))
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (anfe: ActivityNotFoundException) {
            anfe.printStackTrace()
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Consts.WEB_CAFEBAZAAR_URL + context.packageName))
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            catch (anfe: ActivityNotFoundException){
                anfe.printStackTrace()
            }
        }
    }
    private fun showRateDialog(mContext: Context, scope: CoroutineScope, preferences: Preferences) {
        val dialog = Dialog(mContext)
        dialog.setContentView(R.layout.appraterdialog)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.show()
        val logoMain = dialog.findViewById<ImageView>(R.id.logo_main)
        val closeImg = dialog.findViewById<ImageView>(R.id.close_img)
        val positiveBtn = dialog.findViewById<Button>(R.id.positive_btn)
        val negativeBtn = dialog.findViewById<Button>(R.id.negative_btn)
        val laterBtn = dialog.findViewById<Button>(R.id.later_btn)
        try {
            logoMain.setImageResource(R.drawable.logo)
        }
        catch (e: Exception){
            e.printStackTrace()
        }
        closeImg.setOnClickListener { dialog.dismiss() }
        positiveBtn.setOnClickListener {
            val animation: Animation =
                AnimationUtils.loadAnimation(mContext, R.anim.touch_release)
            positiveBtn.startAnimation(animation)
            rateUs(mContext)

            scope.launch {
                preferences.save("dontshowagain", true)
            }

            dialog.dismiss()
        }
        negativeBtn.setOnClickListener {
            val animation: Animation =
                AnimationUtils.loadAnimation(mContext, R.anim.touch_release)
            negativeBtn.startAnimation(animation)
            scope.launch(Dispatchers.IO) {
                preferences.save("dontshowagain", true)
            }
            dialog.dismiss()
        }
        laterBtn.setOnClickListener {
            val animation: Animation =
                AnimationUtils.loadAnimation(mContext, R.anim.touch_release)
            laterBtn.startAnimation(animation)
            scope.launch {
                preferences.save("launch_count", 0)
            }
            dialog.dismiss()
        }
    }
}