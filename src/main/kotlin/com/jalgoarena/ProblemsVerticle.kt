package com.jalgoarena

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future

class ProblemsVerticle : AbstractVerticle() {

    override fun start(future: Future<Void>) {
        vertx.createHttpServer()
                .requestHandler {
                    it.response().end("<h1>Hello from my first Vert.x 3 application</h1>")
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
