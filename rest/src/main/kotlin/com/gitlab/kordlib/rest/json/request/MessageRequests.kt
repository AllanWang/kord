package com.gitlab.kordlib.rest.json.request

import com.gitlab.kordlib.common.entity.Flags
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageCreateRequest(
        val content: String? = null,
        val nonce: String? = null,
        val tts: Boolean? = null,
        val embed: EmbedRequest? = null,
        @SerialName("allowed_mentions")
        val allowedMentions: AllowedMentions? = null
)

@Serializable
data class AllowedMentions(val parse: List<String>, val users: List<String>, val roles: List<String>)

data class MultipartMessageCreateRequest(
        val request: MessageCreateRequest,
        val files: List<Pair<String, java.io.InputStream>> = emptyList()
)

@Serializable
data class EmbedRequest(
        val title: String?,
        val type: String?,
        val description: String?,
        val url: String?,
        val timestamp: String? = null,
        val color: Int? = null,
        val footer: EmbedFooterRequest? = null,
        val image: EmbedImageRequest? = null,
        val thumbnail: EmbedThumbnailRequest? = null,
        val author: EmbedAuthorRequest? = null,
        val fields: List<EmbedFieldRequest>? = null
)


@Serializable
data class EmbedFooterRequest(
        val text: String,
        @SerialName("icon_url")
        val iconUrl: String? = null
)

@Serializable
data class EmbedImageRequest(val url: String)

@Serializable
data class EmbedThumbnailRequest(val url: String)

@Serializable
data class EmbedAuthorRequest(
        val name: String? = null,
        val url: String? = null,
        @SerialName("icon_url")
        val iconUrl: String? = null
)

@Serializable
data class EmbedFieldRequest(
        val name: String,
        val value: String,
        val inline: Boolean? = null
)

@Serializable
data class MessageEditPatchRequest(
        val content: String? = null,
        val embed: EmbedRequest? = null,
        val flags: Flags? = null,
        @SerialName("allowed_mentions")
        val allowedMentions: AllowedMentions? = null
)

@Serializable
data class BulkDeleteRequest(val messages: List<String>)
