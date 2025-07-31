package farsi.toenglish.translation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.util.ArrayList
import farsi.toenglish.translation.R
import farsi.toenglish.translation.modules.ChatMess

class ChatArrayAdapter(context: Context, textViewResourceId: Int) :

    ArrayAdapter<ChatMess?>(context, textViewResourceId) {
    private val mListChatMesses: ArrayList<ChatMess> = ArrayList<ChatMess>()
    override fun add(`object`: ChatMess?) {
        super.add(`object`)
        mListChatMesses.add(`object`!!)
    }



    override fun clear() {
        super.clear()
        mListChatMesses.clear()
    }

    override fun getCount(): Int = mListChatMesses.size

    override fun getItem(index: Int): ChatMess {
        return mListChatMesses[index]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val chatMess: ChatMess = getItem(position)
        //var row = convertView
        //if (row == null){
            val row: View
            val inflater: LayoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


            if (!chatMess.getmTranslate() &&
                        chatMess.getmLeft()){
                row = inflater.inflate(R.layout.chat_left,
                    parent,
                    false)
            }
            else if(chatMess.getmTranslate() &&
                    !chatMess.getmLeft()){
                row = inflater.inflate(
                    R.layout.chat_right,
                    parent,
                    false
                )
            }
            else {
                row = inflater.inflate(R.layout.chat_left,
                    parent,
                    false)
            }

        //}

            val textMessage: TextView =
                row.findViewById<View>(R.id.message) as TextView
            textMessage.text = chatMess.getmMessage()

        return row
    }

}