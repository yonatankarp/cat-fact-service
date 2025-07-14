package com.yonatankarp.cat.fact.service.repository

import com.yonatankarp.cat.fact.client.ports.Fact
import org.jooq.DSLContext
import org.jooq.generated.Tables
import org.springframework.stereotype.Repository

@Repository
class CatFactRepository(
    private val jooq: DSLContext,
) {
    suspend fun storeFacts(fact: Fact): Boolean =
        with(Tables.CAT_FACTS) {
            jooq
                .insertInto(this, HASH, FACT)
                .values(fact.hashCode(), fact.value)
                .onConflict(HASH)
                .doNothing()
                .execute() == 1
        }
}
