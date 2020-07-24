package com.gitlab.kordlib.core.gateway.handler

import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.api.put
import com.gitlab.kordlib.cache.api.putAll
import com.gitlab.kordlib.cache.api.query
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.cache.data.*
import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.core.entity.Presence
import com.gitlab.kordlib.core.event.PresenceUpdateEvent
import com.gitlab.kordlib.core.event.guild.*
import com.gitlab.kordlib.core.event.role.RoleCreateEvent
import com.gitlab.kordlib.core.event.role.RoleDeleteEvent
import com.gitlab.kordlib.core.event.role.RoleUpdateEvent
import com.gitlab.kordlib.core.gateway.MasterGateway
import com.gitlab.kordlib.core.toInstant
import com.gitlab.kordlib.gateway.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toSet
import com.gitlab.kordlib.common.entity.DiscordGuild as GatewayGuild
import com.gitlab.kordlib.core.event.Event as CoreEvent

@Suppress("EXPERIMENTAL_API_USAGE")
internal class GuildEventHandler(
        kord: Kord,
        gateway: MasterGateway,
        cache: DataCache,
        coreEventChannel: SendChannel<CoreEvent>
) : BaseGatewayEventHandler(kord, gateway, cache, coreEventChannel) {

    override suspend fun handle(event: Event, shard: Int) = when (event) {
        is GuildCreate -> handle(event, shard)
        is GuildUpdate -> handle(event, shard)
        is GuildDelete -> handle(event, shard)
        is GuildBanAdd -> handle(event, shard)
        is GuildBanRemove -> handle(event, shard)
        is GuildEmojisUpdate -> handle(event, shard)
        is GuildIntegrationsUpdate -> handle(event, shard)
        is GuildMemberAdd -> handle(event, shard)
        is GuildMemberRemove -> handle(event, shard)
        is GuildMemberUpdate -> handle(event, shard)
        is GuildRoleCreate -> handle(event, shard)
        is GuildRoleUpdate -> handle(event, shard)
        is GuildRoleDelete -> handle(event, shard)
        is GuildMembersChunk -> handle(event, shard)
        is PresenceUpdate -> handle(event, shard)
        is InviteCreate -> handle(event, shard)
        is InviteDelete -> handle(event, shard)
        else -> Unit
    }

    private suspend fun GatewayGuild.cache() {
        for (member in members.orEmpty()) {
            cache.put(MemberData.from(member.user!!.id, id, member))
            cache.put(UserData.from(member.user!!))
        }

        for (role in roles) {
            cache.put(RoleData.from(id, role))
        }

        for (channel in channels.orEmpty()) {
            cache.put(ChannelData.from(channel.copy(guildId = this.id))) //guild id always empty
        }

        for (presence in presences.orEmpty()) {
            cache.put(PresenceData.from(id, presence))
        }

        for (voiceState in voiceStates.orEmpty()) {
            cache.put(VoiceStateData.from(voiceState))
        }
        for (emoji in emojis) {
            cache.put(EmojiData.from(id, emoji.id!!, emoji))
        }
    }

    private suspend fun handle(event: GuildCreate, shard: Int) {
        val data = GuildData.from(event.guild)
        cache.put(data)
        event.guild.cache()

        coreEventChannel.send(GuildCreateEvent(Guild(data, kord), shard))
    }

    private suspend fun handle(event: GuildUpdate, shard: Int) {
        val data = GuildData.from(event.guild)
        cache.put(data)
        event.guild.cache()

        coreEventChannel.send(GuildCreateEvent(Guild(data, kord), shard))
    }

    private suspend fun handle(event: GuildDelete, shard: Int) = with(event.guild) {
        val query = cache.query<GuildData> { GuildData::id eq id.toLong() }

        val old = query.asFlow().map { Guild(it, kord) }.singleOrNull()
        query.remove()

        coreEventChannel.send(GuildDeleteEvent(Snowflake(id), unavailable ?: false, old, kord, shard))
    }

    private suspend fun handle(event: GuildBanAdd, shard: Int) = with(event.ban) {
        val data = UserData.from(user)
        cache.put(user)
        val user = User(data, kord)

        coreEventChannel.send(BanAddEvent(user, Snowflake(guildId), shard))
    }

    private suspend fun handle(event: GuildBanRemove, shard: Int) = with(event.ban) {
        val data = UserData.from(user)
        cache.put(user)
        val user = User(data, kord)

        coreEventChannel.send(BanRemoveEvent(user, Snowflake(guildId), shard))
    }

    private suspend fun handle(event: GuildEmojisUpdate, shard: Int) = with(event.emoji) {
        val guildId = Snowflake(guildId)
        val emojis = emojis.map { GuildEmoji(EmojiData.from(guildId.value, it.id!!, it), kord) }.toSet()

        cache.query<GuildData> { GuildData::id eq guildId.longValue }.update {
            it.copy(emojis = emojis.map { emoji -> emoji.data })
        }

        coreEventChannel.send(EmojisUpdateEvent(guildId, emojis, kord, shard))
    }

    private suspend fun handle(event: GuildIntegrationsUpdate, shard: Int) {
        coreEventChannel.send(IntegrationsUpdateEvent(Snowflake(event.integrations.guildId), kord, shard))
    }

    private suspend fun handle(event: GuildMemberAdd, shard: Int) = with(event.member) {
        val userData = UserData.from(user!!)
        val memberData = MemberData.from(user!!.id, event.member)

        cache.put(userData)
        cache.put(memberData)

        val member = Member(memberData, userData, kord)

        coreEventChannel.send(MemberJoinEvent(member, shard))
    }

    private suspend fun handle(event: GuildMemberRemove, shard: Int) = with(event.member) {
        val userData = UserData.from(user)
        cache.query<UserData> { UserData::id eq userData.id }.remove()
        val user = User(userData, kord)

        coreEventChannel.send(MemberLeaveEvent(user, Snowflake(guildId), shard))
    }

    private suspend fun handle(event: GuildMemberUpdate, shard: Int) = with(event.member) {
        val userData = UserData.from(user)
        cache.put(userData)

        val old = cache.query<MemberData> {
            MemberData::userId eq userData.id
            MemberData::guildId eq guildId.toLong()
        }.asFlow().map { Member(it, userData, kord) }.singleOrNull()

        cache.query<MemberData> {
            MemberData::userId eq userData.id
            MemberData::guildId eq guildId.toLong()
        }.update { it + this }

        val roles = roles.asSequence().map { Snowflake(it) }.toSet()

        coreEventChannel.send(
                MemberUpdateEvent(
                        old,
                        Snowflake(guildId),
                        Snowflake(userData.id),
                        roles,
                        nick ?: userData.username,
                        premiumSince?.toInstant(),
                        kord,
                        shard
                )
        )
    }

    private suspend fun handle(event: GuildRoleCreate, shard: Int) {
        val data = RoleData.from(event.role)
        cache.put(data)

        coreEventChannel.send(RoleCreateEvent(Role(data, kord), shard))
    }

    private suspend fun handle(event: GuildRoleUpdate, shard: Int) {
        val data = RoleData.from(event.role)
        cache.put(data)

        coreEventChannel.send(RoleUpdateEvent(Role(data, kord), shard))
    }

    private suspend fun handle(event: GuildRoleDelete, shard: Int) = with(event.role) {
        val query = cache.query<RoleData> { RoleData::id eq event.role.id.toLong() }

        val old = run {
            val data = query.singleOrNull() ?: return@run null
            Role(data, kord)
        }

        query.remove()

        coreEventChannel.send(RoleDeleteEvent(Snowflake(guildId), Snowflake(id), old, kord, shard))
    }

    private suspend fun handle(event: GuildMembersChunk, shard: Int) = with(event.data) {
        val presences = presences.orEmpty().map { PresenceData.from(guildId, it) }
        cache.putAll(presences)

        val members = members.asFlow().map { member ->
            val memberData = MemberData.from(member.user!!.id, guildId, member)
            cache.put(memberData)
            val userData = UserData.from(member.user!!)
            cache.put(userData)

            Member(memberData, userData, kord)
        }.toSet()

        coreEventChannel.send(MemberChunksEvent(Snowflake(guildId), members, kord, shard))
    }

    private suspend fun handle(event: PresenceUpdate, shard: Int) = with(event.presence) {
        val data = PresenceData.from(this.guildId!!, this)

        val old = cache.query<PresenceData> { PresenceData::id eq data.id }
                .asFlow().map { Presence(it, kord) }.singleOrNull()

        cache.put(data)
        val new = Presence(data, kord)

        val user = cache
                .query<UserData> { UserData::id eq event.presence.user.id.toLong() }
                .singleOrNull()
                ?.let { User(it, kord) }

        coreEventChannel.send(PresenceUpdateEvent(user, this.user, Snowflake(guildId!!), old, new, shard))
    }

    private suspend fun handle(event: InviteCreate, shard: Int) = with(event) {
        val data = InviteCreateData.from(invite)

        with(invite.inviter) {
            cache.query<UserData> { UserData::id eq id.toLong() }
                    .update { it.copy(discriminator = discriminator, username = username, avatar = avatar) }
        }

        coreEventChannel.send(InviteCreateEvent(data, kord, shard))
    }

    private suspend fun handle(event: InviteDelete, shard: Int) = with(event) {
        val data = InviteDeleteData.from(invite)
        coreEventChannel.send(InviteDeleteEvent(data, kord, shard))
    }

}