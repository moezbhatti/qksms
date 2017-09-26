package com.moez.QKSMS.ui.messages

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.moez.QKSMS.R
import com.moez.QKSMS.ui.view.AvatarView

class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val timestamp: TextView = view.findViewById(R.id.timestamp)
    val message: View = view.findViewById(R.id.message)
    val status: TextView? = view.findViewById(R.id.status)
    val avatar: AvatarView? = view.findViewById(R.id.avatar)
    val body: TextView = view.findViewById(R.id.body)

}