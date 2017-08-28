package com.moez.QKSMS.ui.conversations

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import com.moez.QKSMS.R
import com.moez.QKSMS.data.sync.SyncManager
import com.moez.QKSMS.ui.base.QkFragment
import kotlinx.android.synthetic.main.conversation_list_fragment.*
import javax.inject.Inject

class ConversationListFragment : QkFragment() {

    @Inject lateinit var viewModel: ConversationListViewModel

    override fun provideLayoutRes(): Int = R.layout.conversation_list_fragment
    override fun provideMenuRes(): Int = R.menu.conversations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context.getAppComponent()?.inject(this)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        conversationList.layoutManager = LinearLayoutManager(context)
        conversationList.adapter = ConversationAdapter(context, viewModel.conversations)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.load_conversations -> { // TODO: This is for testing, remove this
                progress.visibility = View.VISIBLE
                SyncManager.copyToRealm(context) {
                    progress.visibility = View.GONE
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}