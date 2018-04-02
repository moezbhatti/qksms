package blocking

import android.content.Context
import android.content.pm.PackageManager
import timber.log.Timber
import javax.inject.Inject

class BlockManager @Inject constructor(private val context: Context) {

    fun isBlocked(address: String) {
        val isShouldIAnswerInstalled = try {
            context.packageManager.getApplicationInfo("org.mistergroup.shouldianswerpersonal", 0).enabled
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.w(e)
            false
        }

        if (isShouldIAnswerInstalled) {
            val shouldIAnswerBinder = ShouldIAnswerBinder()
            shouldIAnswerBinder.callback = object : ShouldIAnswerBinder.Callback {

                override fun onNumberRating(number: String?, rating: Int) {
                    Timber.i("onNumberRating " + number + ": " + rating.toString())
                    shouldIAnswerBinder.unbind(context)

                    Timber.v("Should block: ${rating == ShouldIAnswerBinder.RATING_NEGATIVE}")
                }

                override fun onServiceConnected() {
                    try {
                        shouldIAnswerBinder.getNumberRating(address)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

                override fun onServiceDisconnected() {}
            }

            shouldIAnswerBinder.bind(context)
        }
    }

}