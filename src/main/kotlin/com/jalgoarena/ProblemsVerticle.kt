package com.jalgoarena

import com.jalgoarena.domain.Problem
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.ext.web.Router

class ProblemsVerticle : AbstractVerticle() {

    override fun start(future: Future<Void>) {
        val router = Router.router(vertx)

        router.route("/problems").handler { routingContext ->
            val response = routingContext.response()

            val problem = Problem("fib", "Fibonacci", "Fibonacci prob" +
                    "lem", 1, null, null, 1)

            response.putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(problem))
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
