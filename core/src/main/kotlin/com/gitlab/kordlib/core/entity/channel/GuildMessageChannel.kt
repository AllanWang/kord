package com.gitlab.kordlib.core.entity.channel

import com.gitlab.kordlib.common.annotation.KordUnstableApi
import com.gitlab.kordlib.core.behavior.channel.GuildMessageChannelBehavior
import com.gitlab.kordlib.core.supplier.EntitySupplyStrategy

/**
 * An instance of a Discord message channel associated to a [guild].
 */
@OptIn(KordUnstableApi::class)
interface GuildMessageChannel : CategorizableChannel, MessageChannel, GuildMessageChannelBehavior {

    /**
     * The channel topic, if present.
     */
    val topic: String? get() = data.topic

    /**
     * Returns a new [GuildMessageChannel] with the given [strategy].
     */
    override fun withStrategy(strategy: EntitySupplyStrategy<*>): GuildMessageChannel

}