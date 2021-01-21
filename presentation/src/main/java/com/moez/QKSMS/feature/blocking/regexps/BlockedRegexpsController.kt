package com.moez.QKSMS.feature.blocking.regexps

import android.view.View
import androidx.appcompat.app.AlertDialog
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkController
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.databinding.BlockedRegexpsAddDialogBinding
import com.moez.QKSMS.databinding.BlockedRegexpsControllerBinding
import com.moez.QKSMS.injection.appComponent
import com.moez.QKSMS.util.PhoneNumberUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class BlockedRegexpsController : QkController<BlockedRegexpsView, BlockedRegexpsState, BlockedRegexpsPresenter,
        BlockedRegexpsControllerBinding>(BlockedRegexpsControllerBinding::inflate), BlockedRegexpsView {
    @Inject
    override lateinit var presenter: BlockedRegexpsPresenter
    @Inject
    lateinit var colors: Colors

    private val adapter = BlockedRegexpsAdapter()
    private val saveRegexSubject: Subject<String> = PublishSubject.create()

    init {
        appComponent.inject(this)
        retainViewMode = RetainViewMode.RETAIN_DETACH
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.blocked_regexps_title)
        showBackButton(true)
    }

    override fun onViewCreated() {
        super.onViewCreated()
        binding.add.setBackgroundTint(colors.theme().theme)
        binding.add.setTint(colors.theme().textPrimary)
        adapter.emptyView = binding.empty
        binding.regexps.adapter = adapter
    }

    override fun render(state: BlockedRegexpsState) {
        adapter.updateData(state.regexps)
    }

    override fun unblockRegex(): Observable<Long> = adapter.unblockRegex
    override fun addRegex(): Observable<*> = binding.add.clicks()
    override fun saveRegex(): Observable<String> = saveRegexSubject

    override fun showAddDialog() {
        val binding = BlockedRegexpsAddDialogBinding.inflate(activity?.layoutInflater!!)
        val dialog = AlertDialog.Builder(activity!!)
                .setView(binding.root)
                .setPositiveButton(R.string.blocked_regexps_dialog_block) { _, _ ->
                    saveRegexSubject.onNext(binding.input.text.toString())
                }
                .setNegativeButton(R.string.button_cancel) { _, _ -> }
        dialog.show()
    }

}
