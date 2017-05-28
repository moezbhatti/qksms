package com.moez.QKSMS.ui.messages

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.moez.QKSMS.R

class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val body = view.findViewById(R.id.message_body) as TextView

}