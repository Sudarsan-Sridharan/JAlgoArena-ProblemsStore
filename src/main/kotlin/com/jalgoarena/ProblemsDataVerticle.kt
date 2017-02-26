package com.jalgoarena

import com.jalgoarena.domain.Constants
import com.jalgoarena.domain.Problem
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.Json
import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.exodus.entitystore.PersistentEntityStores
import org.slf4j.LoggerFactory

class ProblemsDataVerticle : AbstractVerticle() {

    private val log = LoggerFactory.getLogger(this.javaClass)

    private val LOG = LoggerFactory.getLogger(this.javaClass)
    private lateinit var store: PersistentEntityStore

    override fun start() {
        vertx.executeBlocking<List<Problem>>({ future ->
            initializeDb()
            future.complete(retrieveAllProblems())
        }, { asyncResult -> when {
            asyncResult.succeeded() -> sendProblems(asyncResult.result())
            else -> log.error("Error in processing problems!", asyncResult.cause())
        }})
    }

    private fun sendProblems(problems: List<Problem>) {
        val problemsAsJson = Json.encode(problems)
        vertx.eventBus().publish("problems", problemsAsJson)
    }

    private fun initializeDb() {
        store = PersistentEntityStores.newInstance(Constants.storePath)
    }

    private fun retrieveAllProblems(): List<Problem> {
        return store.computeInReadonlyTransaction {
            it.getAll(Constants.entityType).map { Problem.from(it) }
        }
    }

    override fun stop() {
        try {
            LOG.info("Closing persistent store.")
            store.close()
            LOG.info("Persistent store closed")
        } catch (e: Throwable) {
            LOG.error("Error closing persistent store", e)
        }
    }
}
