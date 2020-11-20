package com.gitlab.kordlib.core.event.guild

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.behavior.GuildBehavior
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.core.event.Event

class MemberLeaveEvent(val user: User, val guildId: Snowflake, override val shard: Int) : Event {

    override val kord: Kord get() = user.kord

    val guild: GuildBehavior get() = GuildBehavior(guildId, kord)

    suspend fun getGuild(): Guild = guild.asGuild()

    suspend fun getGuildOrNull(): Guild? = guild.asGuildOrNull()

    override fun toString(): String {
        return "MemberLeaveEvent(user=$user, guildId=$guildId, shard=$shard)"
    }

}
