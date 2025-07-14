package com.yonatankarp.cat.fact.service.controllers

import com.ninjasquad.springmockk.MockkBean
import com.yonatankarp.cat.fact.client.ports.Fact
import com.yonatankarp.cat.fact.service.request.RequestContext
import com.yonatankarp.cat.fact.service.service.CatFactService
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.time.Duration.Companion.seconds

// @OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [CatFactController::class])
@TestConstructor(autowireMode = AutowireMode.ALL)
class CatFactControllerTest(
    private val mockMvc: MockMvc,
) {
    @MockkBean
    private lateinit var service: CatFactService

    @MockkBean
    private lateinit var requestContext: RequestContext

    @Test
    fun `should serve default amount of facts`() =
        runTest(timeout = 1.seconds) {
            coEvery { service.storeFacts(any()) } returns Unit
            coEvery { requestContext.facts } returns setOf(Fact("fact about cat..."))

            mockMvc
                .get("/api/v1/cat/facts")
                .asyncDispatch()
                .andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.facts[0]") { value("fact about cat...") }
                    }
                }.andDo { print() }
                .andReturn()

            coVerify(exactly = 1) { requestContext.facts }
            coVerify(exactly = 1) { service.storeFacts(any()) }
        }

    @ParameterizedTest
    @MethodSource("getData")
    fun `should serve custom amount of facts`(amountOfFacts: Int) =
        runTest(timeout = 1.seconds) {
            coEvery { service.storeFacts(any()) } returns Unit
            coEvery { requestContext.facts } returns (1..amountOfFacts).map { Fact("Fact $it") }.toSet()

            val result =
                mockMvc
                    .get("/api/v1/cat/facts?max=$amountOfFacts")
                    .asyncDispatch()
                    .andExpect {
                        status { isOk() }
                        content {
                            for (i in 0..<amountOfFacts) {
                                jsonPath("$.facts[$i]") { value("Fact ${i + 1}") }
                            }
                        }
                    }.andDo { print() }
                    .andReturn()

            println(result.response.contentAsString)

            coVerify(exactly = 1) { requestContext.facts }
            coVerify(exactly = 1) { service.storeFacts(any()) }
        }

    companion object {
        @JvmStatic
        fun getData(): List<Arguments> =
            listOf(
                Arguments.of(-1),
                Arguments.of(1),
                Arguments.of(3),
                Arguments.of(10),
            )
    }
}
