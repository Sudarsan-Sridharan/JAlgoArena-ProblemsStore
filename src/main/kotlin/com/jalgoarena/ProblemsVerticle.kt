package com.jalgoarena

import com.jalgoarena.data.XodusProblemsRepository
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import java.util.concurrent.CompletableFuture.supplyAsync

class ProblemsVerticle : AbstractVerticle() {

    private val repository = XodusProblemsRepository()

    override fun start(future: Future<Void>) {
        val router = Router.router(vertx)

        router.route("/problems").handler { routingContext ->
            val response = routingContext.response()

            supplyAsync {
                repository.findAll()
            }.thenAcceptAsync { problems ->
                response.putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(problems))
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
