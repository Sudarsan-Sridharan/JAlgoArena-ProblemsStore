package com.jalgoarena

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(VertxUnitRunner::class)
class ProblemsVerticleSpec {

    private lateinit var vertx: Vertx

    @Before
    fun setUp(context: TestContext) {
        vertx = Vertx.vertx()
        vertx.deployVerticle(ProblemsVerticle::class.java.name, DeploymentOptions().apply { isWorker = true },
                context.asyncAssertSuccess())
    }

    @After
    fun tearDown(context: TestContext) {
        vertx.close(context.asyncAssertSuccess())
    }

    @Test
    fun starts_up(context: TestContext) {
        val async = context.async()

        vertx.createHttpClient().getNow(5002, "localhost", "/") { response ->
            response.handler { body ->
                context.assertTrue(body.toString().contains("Hello"))
                async.complete()
            }
        }
    }
}
