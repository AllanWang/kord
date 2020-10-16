package com.gitlab.kordlib.rest.json.request

import com.gitlab.kordlib.common.entity.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Encoder

@Serializable
data class GuildCreateRequest(
        val name: String,
        val region: String? = null,
        val icon: String? = null,
        @SerialName("verification_level")
        val verificationLevel: VerificationLevel? = null,
        @SerialName("default_message_notifications")
        val defaultNotificationLevel: DefaultMessageNotificationLevel? = null,
        val explicitContentFilter: ExplicitContentFilter? = null,
        val roles: List<GuildRoleCreateRequest>? = null,
        val channels: List<GuildCreateChannelRequest>? = null,
        @SerialName("afk_channel_id")
        val afkChannelId: String? = null,
        @SerialName("afk_timeout")
        val afkTimeout: Int? = null,
        @SerialName("system_channel_id")
        val systemChannelId: String? = null
)

@Serializable
data class GuildCreateChannelRequest(
        val name: String,
        val type: ChannelType? = null,
        val topic: String? = null,
        val bitrate: Int? = null,
        @SerialName("user_limit")
        val userLimit: Int? = null,
        @SerialName("rate_limit_per_user")
        val rateLimitPerUser: Int? = null,
        val position: Int? = null,
        @SerialName("permission_overwrites")
        val permissionOverwrite: List<Overwrite>? = null,
        @SerialName("parent_id")
        val parentId: String? = null,
        val nsfw: Boolean? = null,
        val id: String? = null
)

data class GuildChannelPositionModifyRequest(val swaps: List<Pair<String, Int>>) {

    companion object Serializer : SerializationStrategy<GuildChannelPositionModifyRequest> {
        @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor
            get() = buildSerialDescriptor("GuildChannelPosition", StructureKind.LIST)

        override fun serialize(encoder: Encoder, value: GuildChannelPositionModifyRequest) {
            val positions = value.swaps.map { ChannelPosition(it.first, it.second) }
            ListSerializer(ChannelPosition.serializer()).serialize(encoder, positions)
        }

    }

    @Serializable
    private data class ChannelPosition(val id: String, val position: Int)
}

@Serializable
data class GuildMemberAddRequest(
        @SerialName("access_token") val token: String,
        val nick: String? = null,
        val roles: List<String>? = null,
        val mute: Boolean? = null,
        val deaf: Boolean? = null
)

@Serializable
data class GuildMemberModifyRequest(
        val nick: String? = null,
        val roles: List<String>? = null,
        val mute: Boolean? = null,
        val deaf: Boolean? = null,
        @SerialName("channel_id")
        val channelId: String? = null
)


@Serializable
data class GuildBanAddRequest(
        val reason: String? = null,
        @SerialName("delete-message-days")
        val deleteMessagesDays: Int? = null
)

@Serializable
data class GuildRoleCreateRequest(
        val name: String? = null,
        val permissions: Permissions? = null,
        val color: Int = 0,
        @SerialName("hoist")
        val separate: Boolean = false,
        val mentionable: Boolean = false,
        val id: String? = null
)

data class GuildRolePositionModifyRequest(val swaps: List<Pair<String, Int>>) {

    companion object Serializer : SerializationStrategy<GuildRolePositionModifyRequest> {
        @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
        override val descriptor: SerialDescriptor
            get() = buildSerialDescriptor("GuildRolePosition", StructureKind.LIST)

        override fun serialize(encoder: Encoder, value: GuildRolePositionModifyRequest) {
            val positions = value.swaps.map { RolePosition(it.first, it.second) }
            ListSerializer(RolePosition.serializer()).serialize(encoder, positions)
        }

    }

    @Serializable
    private data class RolePosition(val id: String, val position: Int)
}

@Serializable
data class GuildRoleModifyRequest(
        val name: String? = null,
        val permissions: Permissions? = null,
        val color: Int? = null,
        @SerialName("hoist")
        val separate: Boolean? = null,
        val mentionable: Boolean? = null
)

@Serializable
data class GuildIntegrationCreateRequest(val type: Int, val id: String)

@Serializable
data class GuildIntegrationModifyRequest(
        @SerialName("expire_behavior")
        val expireBehavior: Int? = null,
        @SerialName("expire_grace_period")
        val expirePeriod: Int? = null,
        @SerialName("enable_emoticons")
        val emoticons: Boolean? = null
)

@Serializable
data class GuildEmbedModifyRequest(
        val enabled: Boolean,
        @SerialName("channel_id")
        val channelId: String
)

@Serializable
data class CurrentUserNicknameModifyRequest(val nick: String? = null)

@Serializable
data class GuildModifyRequest(
        val name: String? = null,
        val region: String? = null,
        @SerialName("verification_level")
        val verificationLevel: VerificationLevel? = null,
        @SerialName("default_message_notifications")
        val defaultMessageNotificationLevel: DefaultMessageNotificationLevel? = null,
        @SerialName("explicit_content_filter")
        val contentFilter: ExplicitContentFilter? = null,
        @SerialName("afk_channel_id")
        val afkChannel: String? = null,
        @SerialName("afk_timeout")
        val afkTimeout: Int? = null,
        val icon: String? = null,
        @SerialName("owner_id")
        val ownerId: String? = null,
        val spalsh: String? = null,
        @SerialName("system_channel_id")
        val systemChannelId: String? = null,
        val banner: String? = null,
        @SerialName("preferred_locale")
        val preferredLocale: String? = null,
        @SerialName("public_updates_channel_id")
        val publicUpdatesChannelId: String? = null
)