package com.moez.QKSMS.ui.conversations

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.moez.QKSMS.R
import com.moez.QKSMS.ui.view.GroupAvatarView

class ConversationViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val avatar: GroupAvatarView = view.findViewById(R.id.avatars)
    val title: TextView = view.findViewById(R.id.title)
    val date: TextView = view.findViewById(R.id.date)
    val snippet: TextView = view.findViewById(R.id.snippet)

}
