package com.moez.QKSMS.feature.scheduled

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkThemedActivity
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.scheduled_activity.*
import javax.inject.Inject

class ScheduledActivity : QkThemedActivity(), ScheduledView {

    @Inject lateinit var adapter: ScheduledMessageAdapter
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[ScheduledViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scheduled_activity)
        setTitle(R.string.scheduled_title)
        showBackButton(true)
        viewModel.bindView(this)

        adapter.emptyView = empty
        messages.adapter = adapter
    }

    override fun render(state: ScheduledState) {
        adapter.updateData(state.scheduledMessages)
    }

}