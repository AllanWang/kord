@file:Suppress("EXPERIMENTAL_API_USAGE")

package json

import com.gitlab.kordlib.common.entity.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test


private fun file(name: String): String {
    val loader = ChannelTest::class.java.classLoader
    return loader.getResource("json/guild/$name.json").readText()
}

class GuildTest {

    @Test
    fun `Guild serialization`() {
        val guild = Json.decodeFromString(DiscordGuild.serializer(), file("guild"))

        with(guild) {
            id shouldBe "41771983423143937"
            applicationId shouldBe null
            name shouldBe "Discord Developers"
            icon shouldBe "86e39f7ae3307e811784e2ffd11a7310"
            splash shouldBe null
            ownerId shouldBe "80351110224678912"
            region shouldBe "us-east"
            afkChannelId shouldBe "42072017402331136"
            afkTimeout shouldBe 300
            embedEnabled shouldBe true
            embedChannelId shouldBe "41771983444115456"
            verificationLevel.code shouldBe 1
            defaultMessageNotifications.code shouldBe 0
            explicitContentFilter.code shouldBe 0
            mfaLevel.code shouldBe 0
            widgetEnabled shouldBe false
            widgetChannelId shouldBe "41771983423143937"
            roles shouldBe emptyList()
            emojis shouldBe emptyList()
            features shouldBe listOf(GuildFeature.InviteSplash)
            unavailable shouldBe false
        }

    }
}

@Test
fun `UnavailableGuild serialization`() {
    val guild = Json.decodeFromString(DiscordUnavailableGuild.serializer(), file("unavailableguild"))

    with(guild) {
        id shouldBe "41771983423143937"
        unavailable shouldBe true
    }

}


@Test
fun `GuildMember serialization`() {
    val member = Json.decodeFromString(DiscordGuildMember.serializer(), file("guildmember"))

    with(member) {
        nick shouldBe "NOT API SUPPORT"
        roles shouldBe emptyList()
        joinedAt shouldBe "2015-04-26T06:26:56.936000+00:00"
        deaf shouldBe false
        mute shouldBe false
    }
}


@Test
fun `PartialGuild serialization`() {
    val guild = Json.decodeFromString(DiscordPartialGuild.serializer(), file("partialguild"))

    with(guild) {
        id shouldBe "80351110224678912"
        name shouldBe "1337 Krew"
        icon shouldBe "8342729096ea3675442027381ff50dfe"
        owner shouldBe true
        permissions!!.code shouldBe 36953089
    }

}

