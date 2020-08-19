package com.gitlab.kordlib.core

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Entity
import com.gitlab.kordlib.core.event.Event
import com.gitlab.kordlib.core.event.PresenceUpdateEvent
import com.gitlab.kordlib.core.event.VoiceStateUpdateEvent
import com.gitlab.kordlib.core.event.WebhookUpdateEvent
import com.gitlab.kordlib.core.event.channel.*
import com.gitlab.kordlib.core.event.guild.*
import com.gitlab.kordlib.core.event.message.*
import com.gitlab.kordlib.core.event.role.RoleCreateEvent
import com.gitlab.kordlib.core.event.role.RoleDeleteEvent
import com.gitlab.kordlib.core.event.role.RoleUpdateEvent
import com.gitlab.kordlib.gateway.Intent.*
import com.gitlab.kordlib.gateway.Intents
import com.gitlab.kordlib.gateway.MessageDelete
import com.gitlab.kordlib.gateway.PrivilegedIntent
import com.gitlab.kordlib.rest.request.RestRequestException
import com.gitlab.kordlib.rest.route.Position
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass

internal fun String?.toSnowflakeOrNull(): Snowflake? = when {
    this == null -> null
    else -> Snowflake(this)
}

internal fun Long?.toSnowflakeOrNull(): Snowflake? = when {
    this == null -> null
    else -> Snowflake(this)
}

internal fun String.toInstant() = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(this, Instant::from)
internal fun Int.toInstant() = Instant.ofEpochMilli(toLong())
internal fun Long.toInstant() = Instant.ofEpochMilli(this)

internal inline fun <T> catchNotFound(block: () -> T): T? = try {
    block()
} catch (exception: RestRequestException) {
    if (exception.code == 404) null
    else throw exception
}

fun <T : Entity> Flow<T>.sorted(): Flow<T> = flow {
    for (entity in toList().sorted()) {
        emit(entity)
    }
}

/**
 * The terminal operator that returns the first element emitted by the flow that matches the [predicate]
 * and then cancels flow's collection.
 * Returns `null` if the flow was empty.
 */
suspend inline fun <T : Any> Flow<T>.firstOrNull(crossinline predicate: suspend (T) -> Boolean): T? =
        filter { predicate(it) }.firstOrNull()

/**
 * The terminal operator that returns `true` if any of the elements match [predicate].
 * The flow's collection is cancelled when a match is found.
 */
suspend inline fun <T : Any> Flow<T>.any(crossinline predicate: suspend (T) -> Boolean): Boolean =
        firstOrNull(predicate) != null

/**
 * The non-terminal operator that returns a new flow that will emit values of the second [flow] only after the first
 * flow finished collecting without values.
 *
 * ```kotlin
 * emptyFlow<String>().switchIfEmpty(flowOf("hello", "world")) //["hello", "world"]
 *
 * flowOf("hello", "world").switchIfEmpty(flowOf("goodbye", "world")) //["hello", "world"]
 * ```
 */
internal fun <T> Flow<T>.switchIfEmpty(flow: Flow<T>): Flow<T> = flow {
    var empty = true
    collect {
        empty = false
        emit(it)
    }

    if (empty) {
        flow.collect {
            emit(it)
        }
    }
}

/**
 * The terminal operator that returns the index of the first element emitted by the flow that matches the [predicate]
 * and then cancels flow's collection.
 * Returns `null` if the flow was empty or no element matched the [predicate].
 */
internal suspend fun <T> Flow<T>.indexOfFirstOrNull(predicate: suspend (T) -> Boolean): Int? {
    var counter = 0
    return map {
        val pair = counter to it
        counter += 1
        pair
    }
            .filter { predicate(it.second) }
            .take(1)
            .singleOrNull()?.first
}

internal fun <C : Collection<T>, T> paginate(
        start: String,
        batchSize: Int,
        idSelector: (T) -> String,
        itemSelector: (Collection<T>) -> T?,
        directionSelector: (String) -> Position,
        request: suspend (position: Position) -> C,
): Flow<T> = flow {
    var position = directionSelector(start)
    var size = batchSize

    while (true) {
        val response = request(position)
        for (item in response) emit(item)

        val id = itemSelector(response)?.let(idSelector) ?: break
        position = directionSelector(id)

        if (response.size < size) break
        size = response.size
    }
}


/**
 * Discord returns values in order newest -> oldest (big -> small) (confirmed for messages),
 * meaning that the first item returned is the one last created (youngest) in the batch.
 */
internal fun <T> youngestItem(idSelector: (T) -> String): (Collection<T>) -> T? = function@{
    if (it.size <= 1) return@function it.firstOrNull()

    val first = it.first()
    val last = it.last()

    val firstId = idSelector(first).toLong()
    val lastId = idSelector(last).toLong()

    if (firstId > lastId) first
    else last
}

/**
 * Discord returns values in order oldest -> newest (big -> small) (confirmed for messages),
 * meaning that the last item returned is the one first created (oldest) in the batch.
 */
internal fun <T> oldestItem(idSelector: (T) -> String): (Collection<T>) -> T? = function@{
    if (it.size <= 1) return@function it.firstOrNull()
    val first = it.first()
    val last = it.last()

    val firstId = idSelector(first).toLong()
    val lastId = idSelector(last).toLong()

    if (firstId < lastId) first
    else last
}

/**
 *  Selects the [Position.After] the youngest item in the batch.
 */
internal fun <C : Collection<T>, T> paginateForwards(start: Snowflake = Snowflake("0"), batchSize: Int, idSelector: (T) -> String, request: suspend (position: Position) -> C): Flow<T> =
        paginate(start.value, batchSize, idSelector, youngestItem(idSelector), Position::After, request)

/**
 *  Selects the [Position.After] the youngest item in the batch.
 */
internal fun <C : Collection<T>, T : Entity> paginateForwards(start: Snowflake = Snowflake("0"), batchSize: Int, request: suspend (position: Position) -> C): Flow<T> =
        paginate(start.value, batchSize, { it.id.value }, youngestItem { it.id.value }, Position::After, request)

/**
 *  Selects the [Position.Before] the oldest item in the batch.
 */
internal fun <C : Collection<T>, T> paginateBackwards(start: Snowflake = Snowflake(Long.MAX_VALUE), batchSize: Int, idSelector: (T) -> String, request: suspend (position: Position) -> C): Flow<T> =
        paginate(start.value, batchSize, idSelector, oldestItem(idSelector), Position::Before, request)

/**
 *  Selects the [Position.Before] the oldest item in the batch.
 */
internal fun <C : Collection<T>, T : Entity> paginateBackwards(start: Snowflake = Snowflake(Long.MAX_VALUE), batchSize: Int, request: suspend (position: Position) -> C): Flow<T> =
        paginate(start.value, batchSize, { it.id.value }, oldestItem { it.id.value }, Position::Before, request)

inline fun <reified T : Event> Intents.IntentsBuilder.enableEvent() = enableEvent(T::class)

fun Intents.IntentsBuilder.enableEvents(events: Iterable<KClass<out Event>>) = events.forEach { enableEvent(it) }
fun Intents.IntentsBuilder.enableEvents(vararg events: KClass<out Event>) = events.forEach { enableEvent(it) }

@OptIn(PrivilegedIntent::class)
fun Intents.IntentsBuilder.enableEvent(event: KClass<out Event>) = when (event) {
    GuildCreateEvent::class,
    GuildDeleteEvent::class,
    RoleCreateEvent::class,
    RoleUpdateEvent::class,
    RoleDeleteEvent::class,

    ChannelCreateEvent::class,
    CategoryCreateEvent::class,
    DMChannelCreateEvent::class,
    NewsChannelCreateEvent::class,
    StoreChannelCreateEvent::class,
    TextChannelCreateEvent::class,
    VoiceChannelCreateEvent::class,

    ChannelUpdateEvent::class,
    CategoryUpdateEvent::class,
    DMChannelUpdateEvent::class,
    NewsChannelUpdateEvent::class,
    StoreChannelUpdateEvent::class,
    TextChannelUpdateEvent::class,
    VoiceChannelUpdateEvent::class,

    ChannelDeleteEvent::class,
    CategoryDeleteEvent::class,
    DMChannelDeleteEvent::class,
    NewsChannelDeleteEvent::class,
    StoreChannelDeleteEvent::class,
    TextChannelDeleteEvent::class,
    VoiceChannelDeleteEvent::class,

    ChannelPinsUpdateEvent::class,
    -> {
        +Guilds
        +DirectMessages
    }

    MemberJoinEvent::class, MemberUpdateEvent::class, MemberLeaveEvent::class -> +GuildMembers

    BanAddEvent::class, BanRemoveEvent::class -> +GuildBans

    EmojisUpdateEvent::class -> +GuildEmojis

    IntegrationsUpdateEvent::class -> +GuildIntegrations

    WebhookUpdateEvent::class -> +GuildWebhooks

    InviteCreateEvent::class, InviteDeleteEvent::class -> +GuildInvites

    VoiceStateUpdateEvent::class -> +GuildVoiceStates

    PresenceUpdateEvent::class -> +GuildPresences

    MessageCreateEvent::class, MessageUpdateEvent::class, MessageDelete::class -> {
        +GuildMessages
        +DirectMessages
    }

    ReactionAddEvent::class, ReactionRemoveEvent::class, ReactionRemoveAllEvent::class, ReactionRemoveEmojiEvent::class -> {
        +GuildMessageReactions
        +DirectMessagesReactions
    }

    TypingStartEvent::class -> +GuildMessageTyping

    else -> Unit

}
