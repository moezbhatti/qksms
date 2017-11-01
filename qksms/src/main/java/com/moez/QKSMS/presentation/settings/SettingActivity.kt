package com.moez.QKSMS.presentation.settings

import android.os.Bundle
import com.moez.QKSMS.R
import com.moez.QKSMS.presentation.base.QkActivity

class SettingActivity : QkActivity<SettingsViewModel, SettingsState>(), SettingsView {

    override val viewModelClass = SettingsViewModel::class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setTitle(R.string.title_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.setView(this)
    }

    override fun render(state: SettingsState) {
    }

}