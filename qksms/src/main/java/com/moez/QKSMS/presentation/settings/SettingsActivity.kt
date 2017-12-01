package com.moez.QKSMS.presentation.settings

import android.app.ProgressDialog
import android.os.Bundle
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.presentation.base.QkActivity
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : QkActivity<SettingsViewModel>(), SettingsView {

    override val viewModelClass = SettingsViewModel::class
    override val preferencesAddedIntent by lazy { fragment.preferencesAdded }
    override val preferenceClickIntent by lazy { fragment.preferenceClicks }
    override val preferenceChangeIntent by lazy { fragment.preferenceChanges }

    private lateinit var fragment: SettingsFragment
    private lateinit var progressDialog: ProgressDialog // TODO remove this

    override fun onCreate(savedInstanceState: Bundle?) {
        AppComponentManager.appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setTitle(R.string.title_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        progressDialog = ProgressDialog(this)
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)

        fragment = fragmentManager.findFragmentById(fragmentFrame.id) as? SettingsFragment ?: SettingsFragment()
        viewModel.bindView(this)

        fragmentManager.beginTransaction()
                .replace(fragmentFrame.id, fragment)
                .commit()
    }

    override fun render(state: SettingsState) {
        if (progressDialog.isShowing && !state.syncing) progressDialog.hide()
        else if (!progressDialog.isShowing && state.syncing) progressDialog.show()

        fragment.preferenceManager?.findPreference("defaultSms")?.setSummary(when (state.isDefaultSmsApp) {
            true -> R.string.settings_default_sms_summary_true
            else -> R.string.settings_default_sms_summary_false
        })
    }

}