package com.moez.QKSMS.feature.blocking.regexps

import android.view.ViewGroup
import com.moez.QKSMS.common.base.QkRealmAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.databinding.BlockedRegexListItemBinding
import com.moez.QKSMS.model.BlockedRegex
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

class BlockedRegexpsAdapter : QkRealmAdapter<BlockedRegex, BlockedRegexListItemBinding>() {

    val unblockRegex: Subject<Long> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder<BlockedRegexListItemBinding> {
        return QkViewHolder(parent, BlockedRegexListItemBinding::inflate).apply {
            binding.unblock.setOnClickListener {
                val regex = getItem(adapterPosition) ?: return@setOnClickListener
                unblockRegex.onNext(regex.id)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder<BlockedRegexListItemBinding>, position: Int) {
        val item = getItem(position)!!

        holder.binding.regex.text = item.regex
    }

}
