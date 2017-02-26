package com.jalgoarena

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
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

        vertx.deployVerticle(ProblemsVerticle::class.java.name, deploymentOptions,
                context.asyncAssertSuccess())
    }

    @After
    fun tearDown(context: TestContext) {
        vertx.close(context.asyncAssertSuccess())
    }

    @Test
    fun starts_up(context: TestContext) {
        val async = context.async()

        vertx.createHttpClient().getNow(port, "localhost", "/problems") { response ->
            response.handler { body ->
                context.assertTrue(body.toString().contains("fib"))
                async.complete()
            }
        }
    }
}
