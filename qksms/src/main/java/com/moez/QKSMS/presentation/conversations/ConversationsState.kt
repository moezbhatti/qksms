package com.moez.QKSMS.presentation.conversations

import android.support.v7.widget.RecyclerView

data class ConversationsState(
        val adapter: RecyclerView.Adapter<*>? = null,
        val refreshing: Boolean = false,
        val drawerOpen: Boolean = false
)