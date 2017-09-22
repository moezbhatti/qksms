package com.moez.QKSMS.data.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

open class Conversation : RealmObject() {

    @PrimaryKey var id: Long = 0
    var contacts: RealmList<Contact> = RealmList()
    var messages: RealmList<Message> = RealmList()

    @Ignore var date: Long = 0
        get() = lastMessage?.date ?: 0

    @Ignore var read: Boolean = true
        get() = lastMessage?.read ?: true

    @Ignore var snippet: String = ""
        get () = lastMessage?.body ?: ""

    @Ignore private var lastMessage: Message? = null
        get () = if (messages.size == 0) null else messages[0]

    fun getTitle(): String {
        var title = ""
        contacts.forEachIndexed { index, recipient ->
            title += if (recipient.name.isNotEmpty()) recipient.name else recipient.address
            if (index < contacts.size - 1) {
                title += ", "
            }
        }

        return title
    }
}
