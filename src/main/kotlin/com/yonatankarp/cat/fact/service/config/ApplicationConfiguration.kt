package com.yonatankarp.cat.fact.service.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.yonatankarp.cat.fact.client.ports.CatFactFactory
import com.yonatankarp.cat.fact.client.ports.CatFactProvider
import com.yonatankarp.cat.fact.client.ports.ProviderType
import com.yonatankarp.cat.fact.service.request.RequestContext
import com.yonatankarp.cat.fact.service.request.RequestContextImpl
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Configuration
class ApplicationConfiguration {
    @Bean
    fun catFactProvider(objectMapper: ObjectMapper): CatFactProvider = CatFactFactory.getInstance(ProviderType.API, objectMapper)

    @Bean
    @Scope(WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.INTERFACES)
    fun requestContext(catFactProvider: CatFactProvider): RequestContext =
        runBlocking {
            RequestContextImpl(catFactProvider.get(getMaxFactsNumber()))
        }

    fun getMaxFactsNumber(): Int {
        val servletRequestAttributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        return servletRequestAttributes
            .request
            .getParameterValues("max")
            ?.first()
            ?.toInt()
            ?.coerceIn(minimumValue = MIN_FACTS_NUMBER, maximumValue = MAX_FACTS_NUMBER)
            ?: DEFAULT_FACTS_NUMBER
    }

    companion object {
        private const val DEFAULT_FACTS_NUMBER = 5
        private const val MIN_FACTS_NUMBER = 1
        private const val MAX_FACTS_NUMBER = 10
    }
}
