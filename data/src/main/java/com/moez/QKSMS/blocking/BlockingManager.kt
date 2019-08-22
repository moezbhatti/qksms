package com.moez.QKSMS.blocking

import com.moez.QKSMS.util.Preferences
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Delegates requests to the active blocking client
 */
@Singleton
class BlockingManager @Inject constructor(
    private val prefs: Preferences,
    private val callControlBlockingClient: CallControlBlockingClient,
    private val qksmsBlockingClient: QksmsBlockingClient,
    private val shouldIAnswerBlockingClient: ShouldIAnswerBlockingClient
) : BlockingClient {

    private val client: BlockingClient
        get() = when (prefs.blockingManager.get()) {
            Preferences.BLOCKING_MANAGER_SIA -> shouldIAnswerBlockingClient
            Preferences.BLOCKING_MANAGER_CC -> callControlBlockingClient
            else -> qksmsBlockingClient
        }

    init {
        // Migrate from old SIA preference to blocking manager preference
        if (prefs.sia.get()) {
            prefs.blockingManager.set(Preferences.BLOCKING_MANAGER_SIA)
            prefs.sia.delete()
        }
    }

    override fun isAvailable(): Boolean = client.isAvailable()

    override fun getClientCapability(): BlockingClientCapability = client.getClientCapability()

    override fun isBlocked(address: String): Single<Boolean> = client.isBlocked(address)

    override fun block(addresses: List<String>): Completable = client.block(addresses)

    override fun unblock(addresses: List<String>): Completable = client.unblock(addresses)

    override fun openSettings() = client.openSettings()

}
