package com.jalgoarena

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jalgoarena.domain.Problem
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(VertxUnitRunner::class)
class ProblemsDataVerticleSpec {

    private lateinit var vertx: Vertx

    @Before
    fun setUp(context: TestContext) {
        vertx = Vertx.vertx()

        val deploymentOptions = DeploymentOptions().apply {
            isWorker = true
        }

        vertx.deployVerticle(ProblemsDataVerticle::class.java.name, deploymentOptions,
                context.asyncAssertSuccess())
    }

    @After
    fun tearDown(context: TestContext) {
        vertx.close(context.asyncAssertSuccess())
    }

    @Test
    fun should_return_all_available_problems(context: TestContext) {
        val async = context.async()

        vertx.eventBus().consumer<String>("problems")
                .handler { message ->
                    val body = message.body()
                    try {
                        val problems = toProblemsArray(body)
                        context.assertTrue(problems.size > 50)
                    } catch(e: Throwable) {
                        context.fail(e)
                    } finally {
                        async.complete()
                    }
                }
    }

    private fun toProblemsArray(body: String) =
            jacksonObjectMapper().readValue(body, Array<Problem>::class.java)
}
