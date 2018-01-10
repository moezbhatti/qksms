package presentation.receiver

import android.net.Uri
import com.klinker.android.send_message.MmsReceivedReceiver
import common.di.appComponent
import interactor.SyncMessage
import javax.inject.Inject

class MmsReceivedReceiver : MmsReceivedReceiver() {

    @Inject lateinit var syncMessage: SyncMessage

    init {
        appComponent.inject(this)
    }

    override fun onMessageReceived(messageUri: Uri?) {
        super.onMessageReceived(messageUri)

        messageUri?.let { uri ->
            val pendingResult = goAsync()
            syncMessage.execute(uri) { pendingResult.finish() }
        }
    }

}