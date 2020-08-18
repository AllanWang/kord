package com.gitlab.kordlib.core.entity

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.common.entity.TargetUserType
import com.gitlab.kordlib.common.exception.RequestException
import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.KordObject
import com.gitlab.kordlib.core.behavior.UserBehavior
import com.gitlab.kordlib.core.behavior.channel.ChannelBehavior
import com.gitlab.kordlib.core.cache.data.InviteData
import com.gitlab.kordlib.core.entity.channel.Channel
import com.gitlab.kordlib.core.exception.EntityNotFoundException
import com.gitlab.kordlib.core.supplier.EntitySupplier
import com.gitlab.kordlib.core.supplier.EntitySupplyStrategy
import com.gitlab.kordlib.core.supplier.getChannelOf
import com.gitlab.kordlib.core.supplier.getChannelOfOrNull
import com.gitlab.kordlib.core.toSnowflakeOrNull

/**
 * An instance of a [Discord Invite](https://discord.com/developers/docs/resources/invite).
 */
data class Invite(
        val data: InviteData,
        override val kord: Kord,
        override val supplier: EntitySupplier = kord.defaultSupplier
) : KordObject, Strategizable {

    /**
     * The unique code of this invite.
     */
    val code: String get() = data.code

    /**
     * The id of the channel this invite is associated to.
     */
    val channelId: Snowflake get() = Snowflake(data.channelId)

    /**
     * Returns [PartialGuild] if the invite was made in a guild, or null if not.
     */
    val partialGuild: PartialGuild? get() = data.guild?.let { PartialGuild(it, kord) }

    /**
     * The id of the [Guild] if the invite was made in a guild, or null if not.
     */
    @Deprecated("Moved to the PartialGuild", ReplaceWith("partialGuild?.id"))

    val guildId: Snowflake? get() = partialGuild?.id

    /**
     * The id of the user who created this invite, if present.
     */
    val inviterId: Snowflake? get() = data.inviterId.toSnowflakeOrNull()

    /**
     * The id of the target user this invite is associated to, if present.
     */
    val targetUserId: Snowflake? get() = data.targetUserId.toSnowflakeOrNull()

    /**
     * The behavior of the channel this invite is associated to.
     */
    val channel: ChannelBehavior get() = ChannelBehavior(channelId, kord)


    /**
     * The user behavior of the user who created this invite, if present.
     */
    val inviter: UserBehavior? get() = inviterId?.let { UserBehavior.invoke(it, kord) }

    /**
     * The user behavior of the target user this invite is associated to, if present.
     */
    val targetUser: UserBehavior? get() = targetUserId?.let { UserBehavior(it, kord) }

    /**
     * The type of user target for this invite, if present.
     */
    val targetUserType: TargetUserType? get() = data.targetUserType

    /**
     * Approximate count of members in the channel this invite is associated to, if present.
     */
    val approximateMemberCount: Int? get() = data.approximateMemberCount

    /**
     * Approximate count of members online in the channel this invite is associated to, if present. (only present when the target user isn't null)
     */
    val approximatePresenceCount: Int? get() = data.approximatePresenceCount

    /**
     * Requests to get the channel this invite is for.
     *
     * @throws [RequestException] if anything went wrong during the request.
     * @throws [EntityNotFoundException] if the [Channel] wasn't present.
     */
    suspend fun getChannel(): Channel = supplier.getChannelOf(channelId)

    /**
     * Requests to get the channel this invite is for,
     * returns null if the [Channel] isn't present.
     *
     * @throws [RequestException] if anything went wrong during the request.
     */
    suspend fun getChannelOrNull(): Channel? = supplier.getChannelOfOrNull(channelId)

    /**
     * Requests to get the creator of the invite for,
     * returns null if the [User] isn't present or [inviterId] is null.
     *
     * @throws [RequestException] if anything went wrong during the request.
     */
    suspend fun getInviter(): User? = inviterId?.let { supplier.getUserOrNull(it) }

    /**
     * Requests to get the user this invite was created for,
     * returns null if the [User] isn't present or [targetUserId] is null.
     *
     * @throws [RequestException] if anything went wrong during the request.
     */
    suspend fun getTargetUser(): User? = targetUserId?.let { supplier.getUserOrNull(it) }

    /**
     * Requests to delete the invite.
     */
    suspend fun delete(reason: String? = null) {
        kord.rest.invite.deleteInvite(data.code, reason)
    }

    /**
     * Returns a new [Invite] with the given [strategy].
     */
    override fun withStrategy(strategy: EntitySupplyStrategy<*>): Invite =
            Invite(data, kord, strategy.supply(kord))

}