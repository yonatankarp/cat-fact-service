package com.yonatankarp.cat.fact.service.request

import com.yonatankarp.cat.fact.client.ports.Fact

/**
 * Provides random amount of facts about cats into the request context.
 *
 * This interface is used by SpringBoot to inject the user context into
 * the controller for each call made for the service.
 */
interface RequestContext {
    var facts: Set<Fact>?
}

open class RequestContextImpl(
    override var facts: Set<Fact>? = null,
) : RequestContext
