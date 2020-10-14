package com.gitlab.kordlib.core.event.guild

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.behavior.GuildBehavior
import com.gitlab.kordlib.core.behavior.MemberBehavior
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.Role
import com.gitlab.kordlib.core.entity.Strategizable
import com.gitlab.kordlib.core.event.Event
import com.gitlab.kordlib.core.supplier.EntitySupplier
import com.gitlab.kordlib.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import java.time.Instant

class MemberUpdateEvent(
        val old: Member?,
        val guildId: Snowflake,
        val memberId: Snowflake,
        val currentRoleIds: Set<Snowflake>,
        val currentNickName: String,
        val premiumSince: Instant?,
        override val kord: Kord,
        override val shard: Int,
        override val supplier: EntitySupplier = kord.defaultSupplier
) : Event, Strategizable {

    val member: MemberBehavior get() = MemberBehavior(guildId, memberId, kord)

    val guild: GuildBehavior get() = GuildBehavior(guildId, kord)

    val currentRoles: Flow<Role> get() = supplier
            .getGuildRoles(guildId)
            .filter { it.id in currentRoleIds }

    suspend fun getMember(): Member = supplier.getMember(guildId = guildId, userId = memberId)

    suspend fun getMemberOrNull(): Member? = supplier.getMemberOrNull(guildId = guildId, userId = memberId)

    suspend fun getGuild(): Guild = supplier.getGuild(guildId)

    suspend fun getGuildOrNull(): Guild? = supplier.getGuildOrNull(guildId)

    override fun withStrategy(strategy: EntitySupplyStrategy<*>): MemberUpdateEvent =
            MemberUpdateEvent(
                    old,
                    guildId,
                    memberId,
                    currentRoleIds,
                    currentNickName,
                    premiumSince,
                    kord,
                    shard,
                    strategy.supply(kord)
            )

    override fun toString(): String {
        return "MemberUpdateEvent(old=$old, guildId=$guildId, memberId=$memberId, currentRoleIds=$currentRoleIds, currentNickName='$currentNickName', premiumSince=$premiumSince, kord=$kord, shard=$shard, supplier=$supplier)"
    }
}