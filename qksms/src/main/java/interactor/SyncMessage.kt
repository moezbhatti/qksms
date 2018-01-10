package interactor

import android.net.Uri
import common.util.SyncManager
import data.model.Message
import io.reactivex.Flowable
import javax.inject.Inject

class SyncMessage @Inject constructor(private val syncManager: SyncManager) : Interactor<Uri, Message>() {

    override fun buildObservable(params: Uri): Flowable<Message> {
        return Flowable.just(params)
                .flatMap { uri -> syncManager.syncMessage(uri) }
    }

}