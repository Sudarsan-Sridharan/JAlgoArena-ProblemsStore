package com.jalgoarena

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jalgoarena.domain.Problem
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.ext.web.Router

class ProblemsAPIVerticle : AbstractVerticle() {

    private var problems = emptyList<Problem>()

    override fun start(future: Future<Void>) {
        vertx.deployVerticle(ProblemsDataVerticle::class.java.name, DeploymentOptions().apply { isWorker = true })

        vertx.eventBus().consumer<String>("problems")
                .handler { message ->
                    val body = message.body()
                    val problems = jacksonObjectMapper().readValue(body.toString(), Array<Problem>::class.java)
                    this.problems = problems.toList()
                }

        val router = Router.router(vertx)

        router.route("/problems").handler { routingContext ->
            val response = routingContext.response()

            response.putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encode(problems))
        }

        router.route("/problems/:id").handler { routingContext ->
            val id = routingContext.request().getParam("id")
            val response = routingContext.response()

            val problem = problems.firstOrNull { it.id == id }

            when(problem) {
                null -> response.setStatusCode(404).end()
                else -> response.putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encode(problem))
            }
        }

        vertx.createHttpServer()
                .requestHandler {
                    router.accept(it)
                }
                .listen(config().getInteger("http.port", 5002)) { result ->
                    if (result.succeeded()) {
                        future.complete()
                    } else {
                        future.fail(result.cause())
                    }
                }
    }
}
