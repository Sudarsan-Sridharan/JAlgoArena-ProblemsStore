package com.jalgoarena

import com.jalgoarena.data.XodusProblemsRepository
import com.jalgoarena.domain.Problem
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.ext.web.Router

class ProblemsVerticle : AbstractVerticle() {

    private val repository = XodusProblemsRepository()

    override fun start(future: Future<Void>) {
        val router = Router.router(vertx)

        router.route("/problems").handler { routingContext ->
            val response = routingContext.response()

            vertx.executeBlocking<List<Problem>>({ future ->
                future.complete(repository.findAll())
            }, { asyncResult ->
                when {
                    asyncResult.succeeded() ->
                        response.putHeader("content-type", "application/json; charset=utf-8")
                            .end(Json.encode(asyncResult.result()))
                    else -> response.apply {statusCode = 500}
                }
            })
        }

        router.route("/problems/:id").handler { routingContext ->
            val id = routingContext.request().getParam("id")
            val response = routingContext.response()

            vertx.executeBlocking<Problem>({ future ->
                future.complete(repository.find(id))
            }, { asyncResult ->
                when {
                    asyncResult.succeeded() ->
                        response.putHeader("content-type", "application/json; charset=utf-8")
                                .end(Json.encode(asyncResult.result()))
                    else -> response.apply {statusCode = 500}
                }
            })
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
