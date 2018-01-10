package presentation.receiver

import android.net.Uri
import com.klinker.android.send_message.MmsReceivedReceiver
import timber.log.Timber

class MmsReceivedReceiver : MmsReceivedReceiver() {

    override fun onMessageReceived(messageUri: Uri?) {
        super.onMessageReceived(messageUri)
        Timber.v("onMessageReceived: $messageUri")
    }

}