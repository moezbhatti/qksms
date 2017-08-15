package com.moez.QKSMS.ui.messages

import android.os.Bundle
import com.moez.QKSMS.R
import com.moez.QKSMS.ui.base.QkActivity

class MessageListActivity : QkActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base_activity)

        val threadId = intent.getLongExtra("thread_id", 0)
        supportFragmentManager.beginTransaction().replace(R.id.content_frame, MessageListFragment.newInstance(threadId)).commit()
    }

}