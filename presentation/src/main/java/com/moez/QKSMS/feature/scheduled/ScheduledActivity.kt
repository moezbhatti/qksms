package com.moez.QKSMS.feature.scheduled

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.moez.QKSMS.R
import com.moez.QKSMS.common.QkDialog
import com.moez.QKSMS.common.base.QkThemedActivity
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.scheduled_activity.*
import javax.inject.Inject

class ScheduledActivity : QkThemedActivity(), ScheduledView {

    @Inject lateinit var dialog: QkDialog
    @Inject lateinit var messageAdapter: ScheduledMessageAdapter
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override val messageClickIntent by lazy { messageAdapter.clicks }
    override val messageMenuIntent by lazy { dialog.adapter.menuItemClicks }

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[ScheduledViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scheduled_activity)
        setTitle(R.string.scheduled_title)
        showBackButton(true)
        viewModel.bindView(this)

        dialog.title = getString(R.string.scheduled_options_title)
        dialog.adapter.setData(R.array.scheduled_options)

        messageAdapter.emptyView = empty
        messages.adapter = messageAdapter
    }

    override fun render(state: ScheduledState) {
        messageAdapter.updateData(state.scheduledMessages)
    }

    override fun showMessageOptions() {
        dialog.show(this)
    }

}