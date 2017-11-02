package com.moez.QKSMS.presentation.main

import android.support.v7.widget.RecyclerView

data class MainState(
        val adapter: RecyclerView.Adapter<*>? = null,
        val refreshing: Boolean = false,
        val drawerOpen: Boolean = false
)