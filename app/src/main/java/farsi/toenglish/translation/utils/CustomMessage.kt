@file:Suppress("DEPRECATION")

package farsi.toenglish.translation.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import farsi.toenglish.translation.R

object CustomMessage {
    private var toast: Toast? = null

    fun showToast(message: String, context: Context) {
        toast?.let {
            it.cancel()
            toast = null
        }
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast!!.duration = Toast.LENGTH_SHORT
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

            val inflater = (context as Activity).layoutInflater
            val layout = inflater.inflate(
                R.layout.custom_toast,
                (context).findViewById<View>(R.id.root) as ViewGroup?
            )
            val text: TextView = layout.findViewById<View>(R.id.message) as TextView
            text.text = message
            toast!!.view = layout
        } else {
            toast!!.setText(message)
        }
        toast!!.show()

    }
    fun cancel(){
        toast?.let {
            it.cancel()
            toast = null
        }
    }

}