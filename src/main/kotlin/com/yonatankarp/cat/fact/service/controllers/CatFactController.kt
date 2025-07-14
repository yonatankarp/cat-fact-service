package com.yonatankarp.cat.fact.service.controllers

import com.yonatankarp.cat.fact.client.ports.Fact
import com.yonatankarp.cat.fact.service.request.RequestContext
import com.yonatankarp.cat.fact.service.service.CatFactService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CatFactController(
    private val requestContext: RequestContext,
    private val catFactService: CatFactService,
) {
    @GetMapping("/api/v1/cat/facts")
    suspend fun getCatFacts(): ResponseEntity<FactsResponse> {
        val facts = requestContext.facts ?: throw RuntimeException("Could not read facts")
        catFactService.storeFacts(facts)
        return ok(facts.toResponse())
    }
}

private fun Set<Fact>.toResponse() = FactsResponse(this)

data class FactsResponse(
    val facts: Set<Fact>,
)
