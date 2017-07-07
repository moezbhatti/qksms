package com.moez.QKSMS.ui.conversations

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.moez.QKSMS.R

class ConversationViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val title = view.findViewById(R.id.conversation_title) as TextView
    val snippet = view.findViewById(R.id.conversation_snippet) as TextView

}
