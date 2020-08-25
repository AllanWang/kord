package com.gitlab.kordlib.common.entity

import com.gitlab.kordlib.common.annotation.KordUnstableApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@KordUnstableApi
data class DiscordPinsUpdateData(
        @SerialName("guild_id")
        val guildId: String,
        @SerialName("channel_id")
        val channelId: String,
        @SerialName("last_pin_timestamp")
        val lastPinTimestamp: String? = null
)

@Serializable
@KordUnstableApi
data class DiscordTyping(
        @SerialName("channel_id")
        val channelId: String,
        @SerialName("guild_id")
        val guildId: String? = null,
        @SerialName("user_id")
        val userId: String,
        val timestamp: Long
)

