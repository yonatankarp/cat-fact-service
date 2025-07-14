package com.yonatankarp.cat.fact.service

import com.ninjasquad.springmockk.MockkBean
import com.yonatankarp.cat.fact.client.ports.CatFactProvider
import com.yonatankarp.cat.fact.client.ports.Fact
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.jooq.DSLContext
import org.jooq.generated.Tables
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.time.Duration.Companion.seconds

@SpringBootTest
@Testcontainers
@TestConstructor(autowireMode = AutowireMode.ALL)
@AutoConfigureMockMvc
class CatFactServiceIntegrationTest(
    private val mockMvc: MockMvc,
    private val jooq: DSLContext,
) : AbstractIntegrationTest() {
    @MockkBean
    private lateinit var provider: CatFactProvider

    @Test
    fun `should fetch facts and store them correctly in the database`(): Unit =
        runTest(timeout = 5.seconds) {
            // Given facts
            val catFact = "fact about cat..."
            coEvery { provider.get(any()) } returns setOf(Fact(catFact))

            // When we make a call to the service
            mockMvc
                .get("/api/v1/cat/facts")
                .asyncDispatch()
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.facts[0]") { value(catFact) }
                    }
                }.andDo { print() }
                .andReturn()

            // Then we expect the facts to be correctly stored in the db
            val fact =
                jooq
                    .selectFrom(Tables.CAT_FACTS)
                    .limit(1)
                    .fetchOne()

            assertNotNull(fact)
            assertEquals(catFact, fact!!.fact)
        }
}
