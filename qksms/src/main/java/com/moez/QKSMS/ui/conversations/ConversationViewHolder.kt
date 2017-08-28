package com.moez.QKSMS.ui.conversations

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.moez.QKSMS.R

class ConversationViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val title: TextView = view.findViewById(R.id.title)
    val snippet: TextView = view.findViewById(R.id.snippet)

}
