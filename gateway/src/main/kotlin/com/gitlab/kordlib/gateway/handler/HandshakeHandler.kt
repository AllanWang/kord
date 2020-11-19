package com.gitlab.kordlib.gateway.handler

import com.gitlab.kordlib.common.ratelimit.RateLimiter
import com.gitlab.kordlib.common.ratelimit.consume
import com.gitlab.kordlib.gateway.*
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.flow.Flow

internal class HandshakeHandler(
        flow: Flow<Event>,
        private val send: suspend (Command) -> Unit,
        private val sequence: Sequence,
        private val identifyRateLimiter: RateLimiter
) : Handler(flow, "HandshakeHandler") {

    lateinit var configuration: GatewayConfiguration

    private val session: AtomicRef<String?> = atomic(null)

    private val identify
        get() = configuration.identify

    private val resume
        get() = Resume(configuration.token, session.value!!, sequence.value ?: 0)

    private val sessionStart get() = session.value == null

    override fun start() {
        on<Ready> { event ->
            session.update { event.data.sessionId }
        }

        on<Hello> {
            identifyRateLimiter.consume {
                if (sessionStart) send(identify)
                else send(resume)
            }
        }

        on<Close.SessionReset> {
            session.update { null }
        }
    }
}
