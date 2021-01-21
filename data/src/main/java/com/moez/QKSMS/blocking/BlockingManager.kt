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

    override fun isAvailable(): Boolean = client.isAvailable()

    override fun getClientCapability(): BlockingClient.Capability = client.getClientCapability()

    override fun getActionFromAddress(address: String): Single<BlockingClient.Action> = client.getActionFromAddress(address)

    override fun getActionFromContent(content: String): Single<BlockingClient.Action> = client.getActionFromContent(content)

    override fun blockAddresses(addresses: List<String>): Completable = client.blockAddresses(addresses)

    override fun unblockAddresses(addresses: List<String>): Completable = client.unblockAddresses(addresses)

    override fun blockRegexps(regexps: List<String>): Completable = client.blockRegexps(regexps)

    override fun unblockRegexps(regexps: List<String>): Completable = client.unblockRegexps(regexps)

    override fun openSettings() = client.openSettings()

}
