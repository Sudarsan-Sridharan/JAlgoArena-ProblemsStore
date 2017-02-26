package com.jalgoarena

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jalgoarena.domain.Problem
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.ServerSocket

@RunWith(VertxUnitRunner::class)
class ProblemsVerticleSpec {

    private var port = 0
    private lateinit var vertx: Vertx

    @Before
    fun setUp(context: TestContext) {
        val socket = ServerSocket(0)
        port = socket.localPort
        socket.close()

        vertx = Vertx.vertx()

        val deploymentOptions = DeploymentOptions().apply {
            isWorker = true
            config = JsonObject().put("http.port", port)
        }

        vertx.deployVerticle(ProblemsAPIVerticle::class.java.name, deploymentOptions,
                context.asyncAssertSuccess())
    }

    @After
    fun tearDown(context: TestContext) {
        vertx.close(context.asyncAssertSuccess())
    }

    @Test
    fun returns_fib_problem(context: TestContext) {
        sendProblemsOnEventBus()
        val async = context.async()

        vertx.createHttpClient().getNow(port, "localhost", "/problems/fib") { response ->
            context.assertEquals(response.statusCode(), 200)
            context.assertEquals(response.headers().get("content-type"), "application/json; charset=utf-8")
            response.handler { body ->
                val problem = jacksonObjectMapper().readValue(body.toString(), Problem::class.java)
                context.assertEquals(problem.id, "fib")
                async.complete()
            }
        }
    }

    @Test
    fun returns_all_problems(context: TestContext) {
        sendProblemsOnEventBus()
        val async = context.async()

        vertx.createHttpClient().getNow(port, "localhost", "/problems") { response ->
            context.assertEquals(response.statusCode(), 200)
            context.assertEquals(response.headers().get("content-type"), "application/json; charset=utf-8")
            response.handler { body ->
                val problems = deserializeArrayOfProblems(body)
                context.assertTrue(problems.size == 1)
                async.complete()
            }
        }
    }

    private fun deserializeArrayOfProblems(body: Buffer) =
            jacksonObjectMapper().readValue(body.toString(), Array<Problem>::class.java)

    private fun sendProblemsOnEventBus() {
        val problem = Problem("fib", "Fibonacci", "Description", 1, null, null, 1)
        val problemsAsJson = Json.encode(listOf(problem))

        vertx.eventBus().publish("problems", problemsAsJson)
    }
}
