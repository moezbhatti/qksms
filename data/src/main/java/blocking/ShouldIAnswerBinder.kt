package blocking

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException

class ShouldIAnswerBinder {

    companion object {
        val RATING_UNKNOWN = 0
        val RATING_POSITIVE = 1
        val RATING_NEGATIVE = 2
        val RATING_NEUTRAL = 3

        val GET_NUMBER_RATING = 1

        private var serviceMessenger: Messenger? = null
    }

    var callback: Callback? = null

    private var isBound: Boolean = false
    private val messenger = Messenger(IncomingHandler())

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            serviceMessenger = Messenger(service)
            isBound = true
            callback?.onServiceConnected()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            serviceMessenger = null
            isBound = false
            callback?.onServiceDisconnected()
        }
    }

    interface Callback {
        fun onNumberRating(number: String?, rating: Int)

        fun onServiceConnected()

        fun onServiceDisconnected()
    }

    fun bind(context: Context) {
        val intent = Intent("org.mistergroup.shouldianswerpersonal.PublicService")
        intent.`package` = "org.mistergroup.shouldianswerpersonal"
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbind(context: Context) {
        if (isBound && serviceMessenger != null) {
            context.unbindService(connection)
        }
    }

    @Throws(RemoteException::class)
    fun getNumberRating(number: String) {
        val msg = Message()
        msg.what = 1
        val data = Bundle()
        data.putString("number", number)
        msg.data = data
        msg.replyTo = messenger
        serviceMessenger?.send(msg)
    }

    private inner class IncomingHandler : Handler() {

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GET_NUMBER_RATING -> {
                    val inData = msg.data
                    val number = inData.getString("number")
                    val rating = inData.getInt("rating")
                    callback?.onNumberRating(number, rating)
                }

                else -> super.handleMessage(msg)
            }
        }
    }
}
