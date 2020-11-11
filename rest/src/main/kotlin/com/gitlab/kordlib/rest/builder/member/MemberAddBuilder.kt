package com.gitlab.kordlib.rest.builder.member

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.common.annotation.KordDsl
import com.gitlab.kordlib.rest.builder.RequestBuilder
import com.gitlab.kordlib.rest.json.request.GuildMemberAddRequest

@KordDsl
class MemberAddBuilder : RequestBuilder<GuildMemberAddRequest> {
    lateinit var token: String
    var nickname: String? = null
    val roles: Set<Snowflake> = mutableSetOf()
    var muted: Boolean? = null
    var deafened: Boolean? = null

    override fun toRequest(): GuildMemberAddRequest =
            GuildMemberAddRequest(token, nickname, roles.map { it.asString }, mute = muted, deaf = deafened)

}
