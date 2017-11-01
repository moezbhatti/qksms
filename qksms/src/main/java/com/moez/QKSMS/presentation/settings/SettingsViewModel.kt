package com.moez.QKSMS.presentation.settings

import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.domain.interactor.SyncConversations
import com.moez.QKSMS.presentation.base.QkViewModel
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel : QkViewModel<SettingsView, SettingsState>(SettingsState()) {

    @Inject lateinit var syncConversations: SyncConversations

    init {
        AppComponentManager.appComponent.inject(this)
    }

    override fun bindIntents(view: SettingsView) {
        super.bindIntents(view)

        view.preferenceClickIntent.subscribe {
            Timber.v("Preference click: ${it.key}")

            when (it.key) {
                "sync" -> {
                    newState { it.copy(syncing = true) }
                    syncConversations.execute(Unit, {
                        newState { it.copy(syncing = false) }
                    })
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        syncConversations.dispose()
    }
}