package com.valhallagame.valhalla.recipeserviceserver.controller

import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.test.annotation.FlywayTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("in-memory-db", "mock-client")
class RootControllerIntegrationTest {

    @Autowired
    private val restTemplate: TestRestTemplate? = null

    @Test
    @FlywayTest
    fun startServiceAndPatchTest() {
        val result = restTemplate!!.getForEntity("/", String::class.java)
        assertThat(result).isNotNull
        val expected = "{\"message\": \"pong\"}"
        assertEquals(expected, result.body, false)
    }
}